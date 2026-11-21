package com.example.bth5report.model;
import com.example.bth5report.service.ReportMath;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="report_order_rows")
public class ReportOrderRow {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="report_id",nullable=false)
    private ReportRun report;
    @Column(name="order_id",nullable=false) private Long orderId;
    @Column(name="customer_name",nullable=false) private String customerName;
    @Column(name="order_status",nullable=false,length=20) private String status;
    @Column(name="order_created_at") private LocalDateTime createdAt;
    @Column(name="order_value",nullable=false,precision=19,scale=2) private BigDecimal orderValue;
    @Column(name="counted_as_sale",nullable=false) private boolean countedAsSale;
    @Column(name="sold_quantity",nullable=false) private long soldQuantity;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal revenue;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal cost;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal profit;
    @OneToMany(mappedBy="orderRow",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("id ASC") private List<ReportOrderItemRow> items=new ArrayList<>();
    protected ReportOrderRow() {}
    public ReportOrderRow(ReportRun report, ReportMath.OrderLine o) {
        this.report=report;orderId=o.id();customerName=o.customer();status=o.status();
        createdAt=o.createdAt();orderValue=o.orderValue();countedAsSale=o.countedAsSale();
        soldQuantity=o.sold();revenue=o.revenue();cost=o.cost();profit=o.profit();
        o.items().forEach(i -> items.add(new ReportOrderItemRow(this,i)));
    }
    public Long getOrderId(){return orderId;}
    public String getCustomerName(){return customerName;}
    public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public BigDecimal getOrderValue(){return orderValue;}
    public boolean isCountedAsSale(){return countedAsSale;}
    public long getSoldQuantity(){return soldQuantity;}
    public BigDecimal getRevenue(){return revenue;}
    public BigDecimal getCost(){return cost;}
    public BigDecimal getProfit(){return profit;}
    public List<ReportOrderItemRow> getItems(){return items;}
}
