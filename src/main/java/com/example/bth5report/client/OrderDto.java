package com.example.bth5report.client;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record OrderDto(Long id, String customerName, String status, BigDecimal totalAmount,
                       List<OrderItemDto> items, LocalDateTime createdAt) {
    public OrderDto {
        if (status != null) status = status.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
