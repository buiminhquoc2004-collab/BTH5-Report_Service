package com.example.bth5report.model;
import com.example.bth5report.service.ReportMath;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="report_runs")
public class ReportRun {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="generated_at",nullable=false) private LocalDateTime generatedAt;
    @Column(name="catalog_products",nullable=false) private int catalogProducts;
    @Column(name="total_orders",nullable=false) private int totalOrders;
    @Column(name="completed_orders",nullable=false) private int completedOrders;
    @Column(name="stock_quantity",nullable=false) private long stockQuantity;
    @Column(name="sold_quantity",nullable=false) private long soldQuantity;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal revenue;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal cost;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal profit;
    @OneToMany(mappedBy="report",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("productId ASC") private List<ReportProductRow> products=new ArrayList<>();
    @OneToMany(mappedBy="report",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("orderId ASC") private List<ReportOrderRow> orders=new ArrayList<>();
    protected ReportRun() {}
    public ReportRun(ReportMath.Result m) {
        this.generatedAt=LocalDateTime.now(); this.catalogProducts=m.catalogProducts();
        this.totalOrders=m.totalOrders(); this.completedOrders=m.completedOrders();
        this.stockQuantity=m.stock(); this.soldQuantity=m.sold();
        this.revenue=m.revenue(); this.cost=m.cost(); this.profit=m.profit();
        m.products().forEach(p -> products.add(new ReportProductRow(this,p)));
        m.orders().forEach(o -> orders.add(new ReportOrderRow(this,o)));
    }
    public Long getId(){return id;}
    public LocalDateTime getGeneratedAt(){return generatedAt;}
    public int getCatalogProducts(){return catalogProducts;}
    public int getTotalOrders(){return totalOrders;}
    public int getCompletedOrders(){return completedOrders;}
    public long getStockQuantity(){return stockQuantity;}
    public long getSoldQuantity(){return soldQuantity;}
    public BigDecimal getRevenue(){return revenue;}
    public BigDecimal getCost(){return cost;}
    public BigDecimal getProfit(){return profit;}
    public List<ReportProductRow> getProducts(){return products;}
    public List<ReportOrderRow> getOrders(){return orders;}
}
