package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "TransactionFilterRequest", description = "Filtros opcionais para listagem paginada de transacoes")
public record TransactionFilterRequest(

        @Schema(description = "Data inicial do periodo", example = "2026-04-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "Data final do periodo", example = "2026-04-30")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "Filtra por categoria", example = "22222222-2222-2222-2222-222222222222")
        UUID categoryId,

        @Schema(description = "Filtra por conta", example = "11111111-1111-1111-1111-111111111111")
        UUID accountId,

        @Schema(description = "Filtra por tipo", example = "EXPENSE")
        TransactionType type,

        @Schema(description = "Valor minimo", example = "50.00")
        BigDecimal minAmount,

        @Schema(description = "Valor maximo", example = "500.00")
        BigDecimal maxAmount,

        @Schema(description = "Trecho presente na descricao", example = "mercado")
        String description
) {}
