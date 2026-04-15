package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.entity.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Parâmetros de filtro para listagem paginada de transações.
 * Todos os campos são opcionais — campos nulos são ignorados na Specification.
 */
public record TransactionFilterRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        UUID categoryId,

        UUID accountId,

        TransactionType type,

        BigDecimal minAmount,

        BigDecimal maxAmount,

        String description
) {}
