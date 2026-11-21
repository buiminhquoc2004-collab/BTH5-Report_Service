package com.example.bth5report.dto;
import com.example.bth5report.model.ReportProductRow;
import java.math.BigDecimal;
public record ProductReportResponse(Long productId,String productName,boolean inCatalog,
        Integer stockQuantity,long soldQuantity,BigDecimal unitCost,BigDecimal revenue,
        BigDecimal cost,BigDecimal profit) {
    public static ProductReportResponse from(ReportProductRow p) {
        return new ProductReportResponse(p.getProductId(),p.getProductName(),p.isInCatalog(),
                p.getStockQuantity(),p.getSoldQuantity(),p.getUnitCost(),p.getRevenue(),
                p.getCost(),p.getProfit());
    }
}
