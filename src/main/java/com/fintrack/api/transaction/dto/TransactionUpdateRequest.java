package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "TransactionUpdateRequest", description = "Payload para atualizacao de transacao")
public record TransactionUpdateRequest(

        @Schema(description = "Conta associada a transacao", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "Conta e obrigatoria")
        UUID accountId,

        @Schema(description = "Categoria opcional da transacao", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID categoryId,

        @Schema(description = "Tipo da transacao", example = "INCOME")
        @NotNull(message = "Tipo de transacao e obrigatorio")
        TransactionType type,

        @Schema(description = "Valor da transacao", example = "3500.00")
        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @Schema(description = "Descricao livre da transacao", example = "Salario de abril")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String description,

        @Schema(description = "Data efetiva da transacao", example = "2026-04-05")
        @NotNull(message = "Data e obrigatoria")
        LocalDate date
) {}
