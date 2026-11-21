package com.example.bth5report.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record CostRequest(@NotNull @DecimalMin("0.00") @Digits(integer=13,fraction=2)
                          BigDecimal unitCost) {}
