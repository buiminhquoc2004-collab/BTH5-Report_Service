package com.example.bth5report.dto;
import com.example.bth5report.model.ReportOrderRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record OrderReportResponse(Long orderId,String customerName,String status,LocalDateTime createdAt,
        BigDecimal orderValue,boolean countedAsSale,long soldQuantity,
        BigDecimal revenue,BigDecimal cost,BigDecimal profit,List<OrderItemReportResponse> items) {
    public static OrderReportResponse from(ReportOrderRow o) {
        return new OrderReportResponse(o.getOrderId(),o.getCustomerName(),o.getStatus(),
                o.getCreatedAt(),o.getOrderValue(),o.isCountedAsSale(),o.getSoldQuantity(),
                o.getRevenue(),o.getCost(),o.getProfit(),
                o.getItems().stream().map(OrderItemReportResponse::from).toList());
    }
}
