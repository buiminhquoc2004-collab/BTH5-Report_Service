package com.example.bth5report.dto;
import com.example.bth5report.model.ProductCost;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record CostResponse(Long productId,BigDecimal unitCost,LocalDateTime updatedAt) {
    public static CostResponse from(ProductCost c) {
        return new CostResponse(c.getProductId(),c.getUnitCost(),c.getUpdatedAt());
    }
}
