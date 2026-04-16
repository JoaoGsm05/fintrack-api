package com.fintrack.api.account.dto;

import com.fintrack.api.account.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "AccountUpdateRequest", description = "Payload para atualizacao de conta financeira")
public record AccountUpdateRequest(

        @Schema(description = "Nome exibido da conta", example = "Reserva de Emergencia")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
        String name,

        @Schema(description = "Tipo da conta", example = "SAVINGS")
        @NotNull(message = "Tipo de conta e obrigatorio")
        AccountType type,

        @Schema(description = "Codigo ISO da moeda", example = "BRL")
        @NotBlank(message = "Moeda e obrigatoria")
        @Size(min = 3, max = 3, message = "Moeda deve ter exatamente 3 caracteres (ex: BRL, USD)")
        String currency
) {}
