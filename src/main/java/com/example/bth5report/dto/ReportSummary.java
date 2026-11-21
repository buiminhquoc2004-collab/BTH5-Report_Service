package com.example.bth5report.dto;
import com.example.bth5report.model.ReportRun;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record ReportSummary(Long id,LocalDateTime generatedAt,int catalogProducts,int totalOrders,
                            int completedOrders,long stockQuantity,long soldQuantity,
                            BigDecimal revenue,BigDecimal cost,BigDecimal profit) {
    public static ReportSummary from(ReportRun r) {
        return new ReportSummary(r.getId(),r.getGeneratedAt(),r.getCatalogProducts(),
                r.getTotalOrders(),r.getCompletedOrders(),r.getStockQuantity(),r.getSoldQuantity(),
                r.getRevenue(),r.getCost(),r.getProfit());
    }
}
