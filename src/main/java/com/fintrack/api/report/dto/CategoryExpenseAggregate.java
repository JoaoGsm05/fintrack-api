package com.fintrack.api.report.dto;

import java.math.BigDecimal;

public record CategoryExpenseAggregate(
        String categoryName,
        BigDecimal totalAmount
) {}
