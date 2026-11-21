package com.example.bth5report;

import com.example.bth5report.client.*;
import com.example.bth5report.exception.MissingCostException;
import com.example.bth5report.service.ReportMath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportMathTest {
    private static BigDecimal money(String s){ return new BigDecimal(s); }
    @Test void completedOrdersCountTowardRevenueButConfirmedDoNot() {
        var product=new ProductDto(3L,"Laptop Dell",money("18500000.00"),10);
        var item=new OrderItemDto(3L,"Laptop Dell",money("18500000"),2,money("37000000"));
        var complete=new OrderDto(1L,"A","COMPLETED",money("37000000"),List.of(item),LocalDateTime.now());
        var pending=new OrderDto(2L,"B","CONFIRMED",money("37000000"),List.of(item),LocalDateTime.now());
        var result=ReportMath.calculate(List.of(product),List.of(complete,pending),Map.of(3L,money("14000000")));
        assertEquals(10,result.stock());
        assertEquals(2,result.sold());
        assertEquals(1,result.completedOrders());
        assertEquals(0,money("37000000").compareTo(result.revenue()));
        assertEquals(0,money("28000000").compareTo(result.cost()));
        assertEquals(0,money("9000000").compareTo(result.profit()));
        assertEquals(0,result.orders().get(1).revenue().signum());
        assertFalse(result.orders().get(1).countedAsSale());
    }
    @Test void missingCostForCompletedSalesIsRejected() {
        var item=new OrderItemDto(3L,"Laptop",money("100"),1,money("100"));
        var sold=new OrderDto(2L,"Buyer","COMPLETED",money("100"),List.of(item),null);
        assertThrows(MissingCostException.class,() -> ReportMath.calculate(
                List.of(new ProductDto(3L,"Laptop",money("100"),8)),List.of(sold),Map.of()));
    }
    @Test void noOrdersMeansNoRevenueEvenWithStock() {
        var result=ReportMath.calculate(List.of(new ProductDto(3L,"Laptop",money("100"),8)),List.of(),Map.of());
        assertEquals(8,result.stock());
        assertEquals(0,result.revenue().signum());
        assertNull(result.products().getFirst().unitCost());
    }
    @Test void historicalDeletedProductHasUnknownNotZeroStock() {
        var item=new OrderItemDto(19L,"Old item",money("100"),2,money("200"));
        var order=new OrderDto(1L,"A","COMPLETED",money("200"),List.of(item),null);
        var result=ReportMath.calculate(List.of(),List.of(order),Map.of(19L,money("60")));
        assertFalse(result.products().getFirst().inCatalog());
        assertNull(result.products().getFirst().stock());
        assertEquals(0,money("80").compareTo(result.profit()));
    }
    @Test void tamperedSubtotalDoesNotProduceFalseRevenue() {
        var item=new OrderItemDto(3L,"Laptop",money("100"),2,money("100"));
        var order=new OrderDto(1L,"A","COMPLETED",money("100"),List.of(item),null);
        assertThrows(IllegalArgumentException.class,() -> ReportMath.calculate(List.of(),List.of(order),Map.of(3L,money("60"))));
    }
}
