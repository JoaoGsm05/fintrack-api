package com.fintrack.api.budget.dto;

import com.fintrack.api.budget.entity.BudgetPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID userId,
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        BudgetPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal spentAmount,
        BigDecimal remainingAmount
) {}
