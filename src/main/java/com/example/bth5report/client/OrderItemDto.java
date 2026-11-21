package com.example.bth5report.client;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAlias;
public record OrderItemDto(Long productId, String productName, BigDecimal unitPrice, Integer quantity, @JsonAlias("totalPrice") BigDecimal subtotal) {}
