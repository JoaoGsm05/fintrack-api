package com.fintrack.api.category.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID userId,
        UUID parentId,
        String name,
        String icon,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
