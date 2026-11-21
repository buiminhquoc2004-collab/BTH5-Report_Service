package com.example.bth5report.controller;
import com.example.bth5report.dto.CostRequest;
import com.example.bth5report.dto.CostResponse;
import com.example.bth5report.service.CostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/costs","/api/costs"})
public class CostController {
    private final CostService service;
    public CostController(CostService service){this.service=service;}
    @GetMapping public List<CostResponse> all(){return service.all();}
    @PutMapping("/{productId}")
    public CostResponse upsert(@PathVariable Long productId,@Valid @RequestBody CostRequest request){
        return service.set(productId,request.unitCost());
    }
}
