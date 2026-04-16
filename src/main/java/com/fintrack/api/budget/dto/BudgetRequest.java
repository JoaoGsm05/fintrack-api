package com.fintrack.api.budget.dto;

import com.fintrack.api.budget.entity.BudgetPeriod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetRequest(
        @NotNull(message = "Categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Período é obrigatório")
        BudgetPeriod period,

        @NotNull(message = "Data de início é obrigatória")
        LocalDate startDate,

        @NotNull(message = "Data de fim é obrigatória")
        LocalDate endDate
) {}
