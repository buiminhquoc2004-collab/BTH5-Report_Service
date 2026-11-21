package com.example.bth5report.controller;
import com.example.bth5report.dto.*;
import com.example.bth5report.service.ReportService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/reports","/api/reports"})
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service){this.service=service;}
    @GetMapping public List<ReportSummary> all(){return service.all();}
    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generate(){
        ReportResponse report=service.generate();
        return ResponseEntity.created(URI.create("/reports/"+report.summary().id())).body(report);
    }
    @GetMapping("/latest") public ReportResponse latest(){return service.latest();}
    @GetMapping("/products") public List<ProductReportResponse> latestProducts(){return service.latest().products();}
    @GetMapping("/orders") public List<OrderReportResponse> latestOrders(){return service.latest().orders();}
    @GetMapping("/{id}") public ReportResponse byId(@PathVariable Long id){return service.get(id);}
    @GetMapping("/{id}/products")
    public List<ProductReportResponse> products(@PathVariable Long id){return service.get(id).products();}
    @GetMapping("/{id}/orders")
    public List<OrderReportResponse> orders(@PathVariable Long id){return service.get(id).orders();}
}
