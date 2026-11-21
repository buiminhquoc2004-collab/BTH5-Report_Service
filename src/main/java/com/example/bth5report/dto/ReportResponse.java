package com.example.bth5report.dto;
import com.example.bth5report.model.ReportRun;
import java.util.List;
import java.math.BigDecimal;
public record ReportResponse(ReportSummary summary,
        long totalStockQuantity,long totalSoldQuantity,
        BigDecimal totalRevenue,BigDecimal totalCost,BigDecimal totalProfit,
        List<ProductReportResponse> products,List<OrderReportResponse> orders) {
    public static ReportResponse from(ReportRun r) {
        return new ReportResponse(ReportSummary.from(r),
                r.getStockQuantity(),r.getSoldQuantity(),r.getRevenue(),r.getCost(),r.getProfit(),
                r.getProducts().stream().map(ProductReportResponse::from).toList(),
                r.getOrders().stream().map(OrderReportResponse::from).toList());
    }
}
