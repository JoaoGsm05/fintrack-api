package com.fintrack.api.recurring.dto;

import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "RecurringTransactionResponse", description = "Representacao de transacao recorrente")
public record RecurringTransactionResponse(
        @Schema(example = "55555555-5555-5555-5555-555555555555")
        UUID id,
        @Schema(example = "11111111-1111-1111-1111-111111111111")
        UUID accountId,
        @Schema(example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID categoryId,
        @Schema(example = "EXPENSE")
        TransactionType type,
        @Schema(example = "39.90")
        BigDecimal amount,
        @Schema(example = "Assinatura de streaming")
        String description,
        @Schema(example = "MONTHLY")
        RecurringFrequency frequency,
        @Schema(example = "2026-05-01")
        LocalDate nextOccurrence,
        @Schema(example = "true")
        boolean active
) {}
