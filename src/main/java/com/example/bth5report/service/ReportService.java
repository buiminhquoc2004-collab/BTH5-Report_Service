package com.example.bth5report.service;

import com.example.bth5report.client.OrderClient;
import com.example.bth5report.client.ProductClient;
import com.example.bth5report.dto.*;
import com.example.bth5report.model.ReportRun;
import com.example.bth5report.model.ProductCost;
import com.example.bth5report.repository.ProductCostRepository;
import com.example.bth5report.repository.ReportRunRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final ProductCostRepository costRepository;
    private final ReportRunRepository reportRepository;
    public ReportService(ProductClient products,OrderClient orders,
                         ProductCostRepository costs,ReportRunRepository reports) {
        productClient=products;orderClient=orders;costRepository=costs;reportRepository=reports;
    }
    @Transactional
    public ReportResponse generate() {
        var products=productClient.getProducts();
        var orders=orderClient.getOrders();
        Map<Long,java.math.BigDecimal> costs=costRepository.findAll().stream()
                .collect(Collectors.toMap(ProductCost::getProductId,ProductCost::getUnitCost));
        ReportMath.Result computed=ReportMath.calculate(products,orders,costs);
        ReportRun report=reportRepository.saveAndFlush(new ReportRun(computed));
        return ReportResponse.from(report);
    }
    @Transactional(readOnly=true)
    public ReportResponse get(Long id) {
        return ReportResponse.from(reportRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Report " + id + " not found")));
    }
    @Transactional(readOnly=true)
    public ReportResponse latest() {
        return ReportResponse.from(reportRepository.findFirstByOrderByIdDesc().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"No reports yet. POST /reports/generate first.")));
    }
    @Transactional(readOnly=true)
    public List<ReportSummary> all() {
        return reportRepository.findAll(Sort.by(Sort.Direction.DESC,"id")).stream()
                .map(ReportSummary::from).toList();
    }
}
