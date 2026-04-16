package com.fintrack.api.budget.dto;

import com.fintrack.api.budget.entity.BudgetPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "BudgetRequest", description = "Payload para criacao ou atualizacao de budget")
public record BudgetRequest(
        @Schema(description = "Categoria monitorada pelo budget", example = "22222222-2222-2222-2222-222222222222")
        @NotNull(message = "Categoria e obrigatoria")
        UUID categoryId,

        @Schema(description = "Valor limite do budget", example = "1200.00")
        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal amount,

        @Schema(description = "Periodicidade do budget", example = "MONTHLY")
        @NotNull(message = "Periodo e obrigatorio")
        BudgetPeriod period,

        @Schema(description = "Data inicial de vigencia", example = "2026-04-01")
        @NotNull(message = "Data de inicio e obrigatoria")
        LocalDate startDate,

        @Schema(description = "Data final de vigencia", example = "2026-04-30")
        @NotNull(message = "Data de fim e obrigatoria")
        LocalDate endDate
) {}
