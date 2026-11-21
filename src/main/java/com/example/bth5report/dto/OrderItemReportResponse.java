package com.example.bth5report.dto;
import com.example.bth5report.model.ReportOrderItemRow;
import java.math.BigDecimal;
public record OrderItemReportResponse(Long productId,String productName,int quantity,BigDecimal unitPrice,
        BigDecimal unitCost,BigDecimal orderValue,BigDecimal recognizedRevenue,BigDecimal recognizedCost) {
    public static OrderItemReportResponse from(ReportOrderItemRow row) {
        return new OrderItemReportResponse(row.getProductId(),row.getProductName(),row.getQuantity(),
                row.getUnitPrice(),row.getUnitCost(),row.getOrderValue(),row.getRecognizedRevenue(),
                row.getRecognizedCost());
    }
}
