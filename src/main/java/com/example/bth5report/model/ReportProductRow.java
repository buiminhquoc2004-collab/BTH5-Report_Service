package com.example.bth5report.model;
import com.example.bth5report.service.ReportMath;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="report_product_rows")
public class ReportProductRow {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="report_id",nullable=false)
    private ReportRun report;
    @Column(name="product_id",nullable=false) private Long productId;
    @Column(name="product_name",nullable=false) private String productName;
    @Column(name="in_catalog",nullable=false) private boolean inCatalog;
    @Column(name="stock_quantity") private Integer stockQuantity;
    @Column(name="sold_quantity",nullable=false) private long soldQuantity;
    @Column(name="unit_cost",precision=15,scale=2) private BigDecimal unitCost;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal revenue;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal cost;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal profit;
    protected ReportProductRow() {}
    public ReportProductRow(ReportRun report, ReportMath.ProductLine p) {
        this.report=report;productId=p.id();productName=p.name();inCatalog=p.inCatalog();
        stockQuantity=p.stock();soldQuantity=p.sold();unitCost=p.unitCost();
        revenue=p.revenue();cost=p.cost();profit=p.profit();
    }
    public Long getProductId(){return productId;}
    public String getProductName(){return productName;}
    public boolean isInCatalog(){return inCatalog;}
    public Integer getStockQuantity(){return stockQuantity;}
    public long getSoldQuantity(){return soldQuantity;}
    public BigDecimal getUnitCost(){return unitCost;}
    public BigDecimal getRevenue(){return revenue;}
    public BigDecimal getCost(){return cost;}
    public BigDecimal getProfit(){return profit;}
}
