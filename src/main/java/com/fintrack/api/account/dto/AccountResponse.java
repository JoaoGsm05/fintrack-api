package com.fintrack.api.account.dto;

import com.fintrack.api.account.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "AccountResponse", description = "Representacao de conta financeira")
public record AccountResponse(
        @Schema(example = "11111111-1111-1111-1111-111111111111")
        UUID id,
        @Schema(example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,
        @Schema(example = "Carteira Principal")
        String name,
        @Schema(example = "CHECKING")
        AccountType type,
        @Schema(example = "2500.00")
        BigDecimal balance,
        @Schema(example = "BRL")
        String currency,
        @Schema(example = "2026-04-16T10:15:30")
        LocalDateTime createdAt,
        @Schema(example = "2026-04-16T10:15:30")
        LocalDateTime updatedAt
) {}
