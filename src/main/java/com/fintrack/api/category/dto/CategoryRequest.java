package com.fintrack.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequest(

        /** Null indica categoria raiz. */
        UUID parentId,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 50, message = "Ícone deve ter no máximo 50 caracteres")
        String icon,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Cor deve ser um código hex válido (ex: #FF5733)")
        String color
) {}
