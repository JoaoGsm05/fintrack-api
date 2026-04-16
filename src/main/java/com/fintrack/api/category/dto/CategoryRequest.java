package com.fintrack.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "CategoryRequest", description = "Payload para criacao de categoria")
public record CategoryRequest(

        @Schema(description = "Categoria pai. Nulo indica categoria raiz", example = "null", nullable = true)
        UUID parentId,

        @Schema(description = "Nome da categoria", example = "Alimentacao")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
        String name,

        @Schema(description = "Nome do icone exibido no frontend", example = "utensils")
        @Size(max = 50, message = "Icone deve ter no maximo 50 caracteres")
        String icon,

        @Schema(description = "Cor hexadecimal associada a categoria", example = "#FF5733")
        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Cor deve ser um codigo hex valido (ex: #FF5733)")
        String color
) {}
