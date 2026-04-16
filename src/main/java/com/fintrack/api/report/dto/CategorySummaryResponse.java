package com.fintrack.api.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "CategorySummaryResponse", description = "Resumo consolidado de despesas por categoria")
public record CategorySummaryResponse(
        @Schema(example = "Alimentacao")
        String categoryName,
        @Schema(example = "430.25")
        BigDecimal totalAmount,
        @Schema(description = "Percentual que a categoria representa no total do periodo", example = "37.4")
        Double percentage
) {}
