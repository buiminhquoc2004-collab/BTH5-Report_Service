package com.example.bth5report.service;

import com.example.bth5report.client.OrderDto;
import com.example.bth5report.client.OrderItemDto;
import com.example.bth5report.client.ProductDto;
import com.example.bth5report.exception.MissingCostException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Pure business rules: no DB, HTTP, or Spring dependency. */
public final class ReportMath {
    private ReportMath() {}
    public record ProductLine(Long id, String name, boolean inCatalog, Integer stock,
            long sold, BigDecimal unitCost, BigDecimal revenue, BigDecimal cost, BigDecimal profit) {}
    public record ItemLine(Long productId, String productName, int quantity, BigDecimal unitPrice,
            BigDecimal unitCost, BigDecimal orderValue, BigDecimal revenue, BigDecimal cost) {}
    public record OrderLine(Long id, String customer, String status, LocalDateTime createdAt,
            BigDecimal orderValue, boolean countedAsSale, long sold,
            BigDecimal revenue, BigDecimal cost, BigDecimal profit, List<ItemLine> items) {}
    public record Result(int catalogProducts, int totalOrders, int completedOrders,
            long stock, long sold, BigDecimal revenue, BigDecimal cost, BigDecimal profit,
            List<ProductLine> products, List<OrderLine> orders) {}

    private static class RunningProduct {
        final Long id;
        String name;
        boolean inCatalog;
        Integer stock;
        BigDecimal unitCost;
        long sold;
        BigDecimal revenue=BigDecimal.ZERO;
        BigDecimal cost=BigDecimal.ZERO;
        RunningProduct(Long id, String name, boolean inCatalog, Integer stock, BigDecimal unitCost) {
            this.id=id; this.name=name; this.inCatalog=inCatalog; this.stock=stock; this.unitCost=unitCost;
        }
        ProductLine finish() { return new ProductLine(id,name,inCatalog,stock,sold,unitCost,
                revenue,cost,revenue.subtract(cost)); }
    }
    private static BigDecimal nonnegative(BigDecimal value, String label) {
        if (value==null || value.signum()<0) throw new IllegalArgumentException("Invalid " + label + " from upstream");
        return value;
    }
    private static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException("Invalid upstream data: " + message);
    }
    public static Result calculate(List<ProductDto> products, List<OrderDto> orders,
                                   Map<Long,BigDecimal> costs) {
        Objects.requireNonNull(products); Objects.requireNonNull(orders); Objects.requireNonNull(costs);
        Map<Long, RunningProduct> byProduct = new TreeMap<>();
        long currentStock=0;
        for (ProductDto p: products) {
            require(p != null && p.id()!=null && p.id()>0 && p.name()!=null && !p.name().isBlank()
                    && p.quantity()!=null && p.quantity()>=0, "product");
            nonnegative(p.price(), "product price");
            require(!byProduct.containsKey(p.id()), "duplicate product id " + p.id());
            BigDecimal unitCost=costs.get(p.id());
            if (unitCost!=null) nonnegative(unitCost,"unit cost");
            byProduct.put(p.id(),new RunningProduct(p.id(),p.name(),true,p.quantity(),unitCost));
            currentStock+=p.quantity();
        }
        List<OrderLine> byOrder=new ArrayList<>();
        Set<Long> orderIds=new HashSet<>();
        long sold=0;
        int completed=0;
        BigDecimal revenue=BigDecimal.ZERO, cost=BigDecimal.ZERO;
        for (OrderDto order: orders) {
            require(order!=null && order.id()!=null, "null order");
        }
        for (OrderDto order: orders.stream().sorted(Comparator.comparing(OrderDto::id)).toList()) {
            require(order!=null && order.id()!=null && order.id()>0 && orderIds.add(order.id()),"order id");
            require(order.customerName()!=null && !order.customerName().isBlank(),"customer name");
            require(order.status()!=null && Set.of("PENDING","CONFIRMED","COMPLETED","CANCELLED").contains(order.status()),"order status");
            require(order.items()!=null,"order items");
            boolean counted="COMPLETED".equals(order.status());
            if (counted) completed++;
            BigDecimal orderValue=BigDecimal.ZERO, orderRevenue=BigDecimal.ZERO, orderCost=BigDecimal.ZERO;
            long orderSold=0;
            List<ItemLine> itemLines=new ArrayList<>();
            for (OrderItemDto item: order.items()) {
                require(item!=null && item.productId()!=null && item.productId()>0 && item.quantity()!=null
                        && item.quantity()>0 && item.productName()!=null && !item.productName().isBlank(),"order item");
                nonnegative(item.unitPrice(),"item price");
                nonnegative(item.subtotal(),"item subtotal");
                require(item.subtotal().compareTo(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))==0,
                        "order item subtotal for product " + item.productId());
                orderValue=orderValue.add(item.subtotal());
                BigDecimal unitCost=costs.get(item.productId());
                if (unitCost!=null) nonnegative(unitCost,"unit cost");
                RunningProduct rp=byProduct.get(item.productId());
                if (rp==null) {
                    rp=new RunningProduct(item.productId(),item.productName(),false,null,unitCost);
                    byProduct.put(item.productId(),rp);
                }
                if (counted && unitCost==null) {
                    throw new MissingCostException("Missing unit cost for sold product id " + item.productId()
                            + ". Set PUT /costs/" + item.productId() + " before generating the report.");
                }
                BigDecimal lineRevenue=counted ? item.subtotal() : BigDecimal.ZERO;
                BigDecimal lineCost=counted ? unitCost.multiply(BigDecimal.valueOf(item.quantity())) : BigDecimal.ZERO;
                if (counted) {
                    rp.sold+=item.quantity();
                    rp.revenue=rp.revenue.add(lineRevenue);
                    rp.cost=rp.cost.add(lineCost);
                    orderSold+=item.quantity();
                    orderRevenue=orderRevenue.add(lineRevenue);
                    orderCost=orderCost.add(lineCost);
                }
                itemLines.add(new ItemLine(item.productId(),item.productName(),item.quantity(),item.unitPrice(),
                        unitCost,item.subtotal(),lineRevenue,lineCost));
            }
            require(order.totalAmount()!=null && order.totalAmount().compareTo(orderValue)==0,
                    "order total for id " + order.id());
            sold+=orderSold;
            revenue=revenue.add(orderRevenue);
            cost=cost.add(orderCost);
            byOrder.add(new OrderLine(order.id(),order.customerName(),order.status(),order.createdAt(),
                    orderValue,counted,orderSold,orderRevenue,orderCost,
                    orderRevenue.subtract(orderCost),List.copyOf(itemLines)));
        }
        return new Result(products.size(), orders.size(), completed, currentStock, sold,
                revenue,cost,revenue.subtract(cost),
                byProduct.values().stream().map(RunningProduct::finish).toList(), List.copyOf(byOrder));
    }
}
