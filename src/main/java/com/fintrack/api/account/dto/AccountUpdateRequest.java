package com.fintrack.api.account.dto;

import com.fintrack.api.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,

        @NotNull(message = "Tipo de conta é obrigatório")
        AccountType type,

        @NotBlank(message = "Moeda é obrigatória")
        @Size(min = 3, max = 3, message = "Moeda deve ter exatamente 3 caracteres (ex: BRL, USD)")
        String currency
) {}
