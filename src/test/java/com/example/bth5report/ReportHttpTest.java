package com.example.bth5report;

import com.example.bth5report.repository.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportHttpTest {
    static final HttpServer source;
    static volatile boolean productDown;
    static volatile String productToken;
    static volatile String orderToken;
    static {
        try {
            source=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            source.createContext("/auth", x->{
                x.sendResponseHeaders("Bearer sample-token".equals(x.getRequestHeaders().getFirst("Authorization"))?200:401,-1);x.close();
            });
            source.createContext("/products", x->{
                productToken=x.getRequestHeaders().getFirst("Authorization");
                byte[] body="[{\"id\":1,\"name\":\"Keyboard\",\"price\":100000,\"quantity\":8}]".getBytes(StandardCharsets.UTF_8);
                x.getResponseHeaders().set("Content-Type","application/json");
                x.sendResponseHeaders(productDown?503:200,body.length);x.getResponseBody().write(body);x.close();
            });
            source.createContext("/orders", x->{
                orderToken=x.getRequestHeaders().getFirst("Authorization");
                String item="{\"productId\":1,\"productName\":\"Keyboard\",\"unitPrice\":100000,\"quantity\":2,\"totalPrice\":200000}";
                String data="[{\"id\":1,\"customerName\":\"A\",\"status\":\"completed\",\"totalAmount\":200000,\"items\":["+item+"]},{\"id\":2,\"customerName\":\"B\",\"status\":\"pending\",\"totalAmount\":200000,\"items\":["+item+"]},{\"id\":3,\"customerName\":\"C\",\"status\":\"cancelled\",\"totalAmount\":200000,\"items\":["+item+"]}]";
                byte[] body=data.getBytes(StandardCharsets.UTF_8);
                x.getResponseHeaders().set("Content-Type","application/json");x.sendResponseHeaders(200,body.length);x.getResponseBody().write(body);x.close();
            });
            source.start();
        } catch(Exception e){throw new ExceptionInInitializerError(e);}
    }
    @DynamicPropertySource static void configure(DynamicPropertyRegistry r){
        String url="http://127.0.0.1:"+source.getAddress().getPort();
        r.add("auth.service.base-url",()->url);r.add("product.service.base-url",()->url);r.add("order.service.base-url",()->url);
        r.add("spring.datasource.url",()->"jdbc:h2:mem:reporthttp;MODE=MySQL;DB_CLOSE_DELAY=-1");
    }
    @LocalServerPort int port;
    @Autowired ReportRunRepository reports;
    @Autowired ProductCostRepository costs;
    RestClient http;
    @BeforeEach void setup(){reports.deleteAll();costs.deleteAll();productDown=false;productToken=null;orderToken=null;http=RestClient.create("http://localhost:"+port);}
    @AfterAll static void stop(){source.stop(0);}
    ResponseEntity<Map> request(HttpMethod method,String path,String body,String token){
        var req=http.method(method).uri(path);
        if(token!=null)req.header("Authorization",token);
        if(body!=null)req.contentType(MediaType.APPLICATION_JSON).body(body);
        return req.exchange((r,s)->ResponseEntity.status(s.getStatusCode()).body(s.bodyTo(Map.class)));
    }
    ResponseEntity<Map> request(HttpMethod method,String path,String body){return request(method,path,body,"Bearer sample-token");}
    void setCost(int amount){assertEquals(200,request(HttpMethod.PUT,"/costs/1","{\"unitCost\":"+amount+"}").getStatusCode().value());}
    @Test void authenticationIsRequired(){
        assertEquals(401,request(HttpMethod.GET,"/reports/latest",null,null).getStatusCode().value());
        assertEquals(401,request(HttpMethod.GET,"/reports/latest",null,"Bearer wrong").getStatusCode().value());
    }
    @Test void readsRealOrderContractAndPersistsReport(){
        setCost(60000);
        var result=request(HttpMethod.POST,"/reports/generate",null);
        assertEquals(201,result.getStatusCode().value());
        Map summary=(Map)result.getBody().get("summary");
        assertEquals(8,((Number)summary.get("stockQuantity")).intValue());
        assertEquals(2,((Number)summary.get("soldQuantity")).intValue());
        assertEquals(200000,((Number)summary.get("revenue")).intValue());
        assertEquals(120000,((Number)summary.get("cost")).intValue());
        assertEquals(80000,((Number)summary.get("profit")).intValue());
        assertEquals("Bearer sample-token",productToken);assertEquals("Bearer sample-token",orderToken);
        assertEquals(1,reports.count());
        assertEquals(200,request(HttpMethod.GET,"/reports/"+summary.get("id"),null).getStatusCode().value());
    }
    @Test void missingCostRejectsReportWithoutSaving(){
        assertEquals(422,request(HttpMethod.POST,"/reports/generate",null).getStatusCode().value());assertEquals(0,reports.count());
    }
    @Test void upstreamFailureDoesNotSaveIncompleteReport(){
        setCost(60000);productDown=true;
        assertEquals(503,request(HttpMethod.POST,"/reports/generate",null).getStatusCode().value());assertEquals(0,reports.count());
    }
    @Test void storedSnapshotIsUnchangedWhenCostsChange(){
        setCost(60000);
        Map first=(Map)request(HttpMethod.POST,"/reports/generate",null).getBody().get("summary");
        setCost(90000);
        Map second=(Map)request(HttpMethod.POST,"/reports/generate",null).getBody().get("summary");
        assertEquals(20000,((Number)second.get("profit")).intValue());
        Map previous=(Map)request(HttpMethod.GET,"/reports/"+first.get("id"),null).getBody().get("summary");
        assertEquals(80000,((Number)previous.get("profit")).intValue());
        assertEquals(2,reports.count());
    }
}
