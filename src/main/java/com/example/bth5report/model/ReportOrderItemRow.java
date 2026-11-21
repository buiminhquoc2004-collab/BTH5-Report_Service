package com.example.bth5report.model;
import com.example.bth5report.service.ReportMath;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="report_order_item_rows")
public class ReportOrderItemRow {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_row_id",nullable=false)
    private ReportOrderRow orderRow;
    @Column(name="product_id",nullable=false) private Long productId;
    @Column(name="product_name",nullable=false) private String productName;
    @Column(nullable=false) private int quantity;
    @Column(name="unit_price",nullable=false,precision=15,scale=2) private BigDecimal unitPrice;
    @Column(name="unit_cost",precision=15,scale=2) private BigDecimal unitCost;
    @Column(name="order_value",nullable=false,precision=19,scale=2) private BigDecimal orderValue;
    @Column(name="recognized_revenue",nullable=false,precision=19,scale=2) private BigDecimal recognizedRevenue;
    @Column(name="recognized_cost",nullable=false,precision=19,scale=2) private BigDecimal recognizedCost;
    protected ReportOrderItemRow() {}
    public ReportOrderItemRow(ReportOrderRow row,ReportMath.ItemLine item) {
        orderRow=row;productId=item.productId();productName=item.productName();quantity=item.quantity();
        unitPrice=item.unitPrice();unitCost=item.unitCost();orderValue=item.orderValue();
        recognizedRevenue=item.revenue();recognizedCost=item.cost();
    }
    public Long getProductId(){return productId;}
    public String getProductName(){return productName;}
    public int getQuantity(){return quantity;}
    public BigDecimal getUnitPrice(){return unitPrice;}
    public BigDecimal getUnitCost(){return unitCost;}
    public BigDecimal getOrderValue(){return orderValue;}
    public BigDecimal getRecognizedRevenue(){return recognizedRevenue;}
    public BigDecimal getRecognizedCost(){return recognizedCost;}
}
