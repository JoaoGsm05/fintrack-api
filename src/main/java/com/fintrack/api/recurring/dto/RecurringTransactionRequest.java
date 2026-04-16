package com.fintrack.api.recurring.dto;

import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "RecurringTransactionRequest", description = "Payload para criar ou atualizar uma recorrencia")
public record RecurringTransactionRequest(

        @Schema(description = "Conta associada a recorrencia", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "Conta e obrigatoria")
        UUID accountId,

        @Schema(description = "Categoria opcional da recorrencia", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID categoryId,

        @Schema(description = "Tipo da transacao gerada", example = "EXPENSE")
        @NotNull(message = "Tipo de transacao e obrigatorio")
        TransactionType type,

        @Schema(description = "Valor fixo da recorrencia", example = "39.90")
        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @Schema(description = "Descricao livre da recorrencia", example = "Assinatura de streaming")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String description,

        @Schema(description = "Frequencia de geracao", example = "MONTHLY")
        @NotNull(message = "Frequencia e obrigatoria")
        RecurringFrequency frequency,

        @Schema(description = "Data da proxima ocorrencia", example = "2026-05-01")
        @NotNull(message = "Data da proxima ocorrencia e obrigatoria")
        LocalDate nextOccurrence
) {}
