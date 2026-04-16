package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "TransactionResponse", description = "Representacao de uma transacao")
public record TransactionResponse(
        @Schema(example = "33333333-3333-3333-3333-333333333333")
        UUID id,
        @Schema(example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,
        @Schema(example = "11111111-1111-1111-1111-111111111111")
        UUID accountId,
        @Schema(example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID categoryId,
        @Schema(example = "EXPENSE")
        TransactionType type,
        @Schema(example = "89.90")
        BigDecimal amount,
        @Schema(example = "Compra no mercado")
        String description,
        @Schema(example = "2026-04-15")
        LocalDate date,
        @Schema(example = "2026-04-15T18:30:00")
        LocalDateTime createdAt,
        @Schema(example = "2026-04-15T18:30:00")
        LocalDateTime updatedAt
) {}
