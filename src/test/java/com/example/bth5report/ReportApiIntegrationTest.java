package com.example.bth5report;

import com.example.bth5report.client.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportApiIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProductClient productClient;
    @MockitoBean OrderClient orderClient;
    @MockitoBean com.example.bth5report.security.AuthClient authClient;
    @Autowired org.springframework.web.context.WebApplicationContext context;
    @Autowired com.example.bth5report.security.AuthFilter authFilter;
    @Autowired com.example.bth5report.repository.ReportRunRepository reports;
    @Autowired com.example.bth5report.repository.ProductCostRepository costs;
    @BeforeEach void setup() {
        reports.deleteAll(); costs.deleteAll();
        when(authClient.valid("Bearer test-token")).thenReturn(true);
        mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                .addFilters(authFilter)
                .defaultRequest(get("/").header("Authorization", "Bearer test-token")).build();
        when(productClient.getProducts()).thenReturn(List.of(new ProductDto(
                3L,"Laptop",new BigDecimal("18500000"),10)));
        when(orderClient.getOrders()).thenReturn(List.of(new OrderDto(
                1L,"Nguyen Van A","COMPLETED",new BigDecimal("37000000"),
                List.of(new OrderItemDto(3L,"Laptop",new BigDecimal("18500000"),2,
                        new BigDecimal("37000000"))),LocalDateTime.of(2026,11,20,9,0))));
    }
    @Test void costAndReportAreSavedAndReadable() throws Exception {
        mvc.perform(put("/costs/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"unitCost\":14000000}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unitCost").value(14000000));
        mvc.perform(post("/reports/generate"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary.soldQuantity").value(2))
                .andExpect(jsonPath("$.summary.stockQuantity").value(10))
                .andExpect(jsonPath("$.summary.revenue").value(37000000))
                .andExpect(jsonPath("$.summary.cost").value(28000000))
                .andExpect(jsonPath("$.summary.profit").value(9000000))
                .andExpect(jsonPath("$.totalProfit").value(9000000))
                .andExpect(jsonPath("$.products[0].productId").value(3))
                .andExpect(jsonPath("$.orders[0].countedAsSale").value(true));
        mvc.perform(get("/reports/latest"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.orders[0].orderId").value(1));
        mvc.perform(get("/reports/products"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].soldQuantity").value(2));
        mvc.perform(get("/reports/orders"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].profit").value(9000000));
    }
    @Test void invalidCostRejected() throws Exception {
        mvc.perform(put("/costs/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"unitCost\":-1}"))
                .andExpect(status().isBadRequest());
    }
}
