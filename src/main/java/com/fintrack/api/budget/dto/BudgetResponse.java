package com.fintrack.api.budget.dto;

import com.fintrack.api.budget.entity.BudgetPeriod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "BudgetResponse", description = "Representacao de budget com valores consolidados")
public record BudgetResponse(
        @Schema(example = "44444444-4444-4444-4444-444444444444")
        UUID id,
        @Schema(example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,
        @Schema(example = "22222222-2222-2222-2222-222222222222")
        UUID categoryId,
        @Schema(example = "Alimentacao")
        String categoryName,
        @Schema(example = "1200.00")
        BigDecimal amount,
        @Schema(example = "MONTHLY")
        BudgetPeriod period,
        @Schema(example = "2026-04-01")
        LocalDate startDate,
        @Schema(example = "2026-04-30")
        LocalDate endDate,
        @Schema(example = "430.25")
        BigDecimal spentAmount,
        @Schema(example = "769.75")
        BigDecimal remainingAmount
) {}
