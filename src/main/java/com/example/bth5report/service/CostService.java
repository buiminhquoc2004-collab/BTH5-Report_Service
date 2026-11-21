package com.example.bth5report.service;
import com.example.bth5report.dto.CostResponse;
import com.example.bth5report.model.ProductCost;
import com.example.bth5report.repository.ProductCostRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CostService {
    private final ProductCostRepository repository;
    public CostService(ProductCostRepository repository){this.repository=repository;}
    @Transactional
    public CostResponse set(Long productId, BigDecimal unitCost) {
        if(productId==null || productId<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid product ID");
        if(unitCost==null || unitCost.signum()<0 || unitCost.scale()>2 || unitCost.precision()-unitCost.scale()>13)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid unit cost");
        BigDecimal normalized=unitCost.setScale(2,RoundingMode.UNNECESSARY);
        ProductCost cost=repository.findById(productId).orElseGet(() -> new ProductCost(productId,normalized));
        cost.change(normalized);
        return CostResponse.from(repository.save(cost));
    }
    @Transactional(readOnly=true)
    public List<CostResponse> all() {
        return repository.findAll(Sort.by("productId")).stream().map(CostResponse::from).toList();
    }
}
