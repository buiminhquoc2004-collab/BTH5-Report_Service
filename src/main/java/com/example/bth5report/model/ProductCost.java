package com.example.bth5report.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="product_costs")
public class ProductCost {
    @Id @Column(name="product_id")
    private Long productId;
    @Column(name="unit_cost", nullable=false, precision=15, scale=2)
    private BigDecimal unitCost;
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
    protected ProductCost() {}
    public ProductCost(Long productId, BigDecimal unitCost) {
        this.productId=productId;
        change(unitCost);
    }
    public void change(BigDecimal newCost) {
        this.unitCost=newCost;
        this.updatedAt=LocalDateTime.now();
    }
    public Long getProductId(){return productId;}
    public BigDecimal getUnitCost(){return unitCost;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
}
