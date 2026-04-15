package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate date,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
