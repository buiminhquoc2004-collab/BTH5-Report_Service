package com.example.bth5report.client;

import java.util.List;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.example.bth5report.exception.UpstreamException;

@Component
public class OrderClient {
    private final RestClient client;
    public OrderClient(@Value("${order.service.base-url}") String baseUrl) {
        var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
    public List<OrderDto> getOrders() {
        try {
            List<OrderDto> response = client.get().uri("/orders").header("Authorization", RequestToken.current()).retrieve()
                    .body(new ParameterizedTypeReference<List<OrderDto>>() {});
            if (response == null) throw new UpstreamException("Order Service returned no data");
            return response;
        } catch (RestClientException e) {
            throw new UpstreamException("Order Service (8082) unavailable or response invalid", e);
        }
    }
}
