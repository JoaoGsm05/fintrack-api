package com.fintrack.api.recurring.dto;

import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        RecurringFrequency frequency,
        LocalDate nextOccurrence,
        boolean active
) {}
