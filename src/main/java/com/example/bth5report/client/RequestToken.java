package com.example.bth5report.client;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestToken {
    private RequestToken() {}
    public static String current() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes request) {
            String value = request.getRequest().getHeader("Authorization");
            return value == null ? "" : value;
        }
        return "";
    }
}
