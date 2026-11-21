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
public class ProductClient {
    private final RestClient client;
    public ProductClient(@Value("${product.service.base-url}") String baseUrl) {
        var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
    public List<ProductDto> getProducts() {
        try {
            List<ProductDto> response = client.get().uri("/products").header("Authorization", RequestToken.current()).retrieve()
                    .body(new ParameterizedTypeReference<List<ProductDto>>() {});
            if (response == null) throw new UpstreamException("Product Service returned no data");
            return response;
        } catch (RestClientException e) {
            throw new UpstreamException("Product Service (8081) unavailable or response invalid", e);
        }
    }
}
