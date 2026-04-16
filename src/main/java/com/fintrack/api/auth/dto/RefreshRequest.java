package com.fintrack.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshRequest", description = "Payload para renovacao do access token")
public record RefreshRequest(
        @Schema(description = "Refresh token emitido anteriormente", example = "eyJhbGciOiJIUzI1NiJ9.refresh.token")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
