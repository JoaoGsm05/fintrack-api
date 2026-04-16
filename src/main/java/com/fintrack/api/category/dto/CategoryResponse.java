package com.fintrack.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "CategoryResponse", description = "Representacao de categoria")
public record CategoryResponse(
        @Schema(example = "22222222-2222-2222-2222-222222222222")
        UUID id,
        @Schema(example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,
        @Schema(example = "null", nullable = true)
        UUID parentId,
        @Schema(example = "Alimentacao")
        String name,
        @Schema(example = "utensils")
        String icon,
        @Schema(example = "#FF5733")
        String color,
        @Schema(example = "2026-04-16T10:15:30")
        LocalDateTime createdAt,
        @Schema(example = "2026-04-16T10:15:30")
        LocalDateTime updatedAt
) {}
