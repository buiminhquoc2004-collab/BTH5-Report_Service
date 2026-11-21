package com.example.bth5report.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import com.example.bth5report.exception.UpstreamException;

@Component
public class AuthClient {
    private final RestClient client;
    public AuthClient(@Value("${auth.service.base-url}") String url) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public boolean valid(String token) {
        try {
            client.get().uri("/auth").header("Authorization", token).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            return false;
        } catch (RestClientException e) {
            throw new UpstreamException("Authentication Service unavailable", e);
        }
    }
}
