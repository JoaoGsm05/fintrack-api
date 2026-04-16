package com.fintrack.api.report.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        String categoryName,
        BigDecimal totalAmount,
        Double percentage
) {}
