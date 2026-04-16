package com.fintrack.api.recurring.dto;

import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionRequest(

        @NotNull(message = "Conta é obrigatória")
        UUID accountId,

        /** Nullable — transação pode não ter categoria. */
        UUID categoryId,

        @NotNull(message = "Tipo de transação é obrigatório")
        TransactionType type,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description,

        @NotNull(message = "Frequência é obrigatória")
        RecurringFrequency frequency,

        @NotNull(message = "Data da próxima ocorrência é obrigatória")
        LocalDate nextOccurrence
) {}
