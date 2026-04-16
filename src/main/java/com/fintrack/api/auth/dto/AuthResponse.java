package com.fintrack.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Resposta padrao de autenticacao com tokens JWT")
public record AuthResponse(
        @Schema(description = "JWT usado nas rotas protegidas", example = "eyJhbGciOiJIUzI1NiJ9.access.token")
        String accessToken,
        @Schema(description = "Token usado para renovar a sessao", example = "eyJhbGciOiJIUzI1NiJ9.refresh.token")
        String refreshToken,
        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,
        @Schema(description = "Tempo de expiracao do access token em segundos", example = "900")
        long expiresIn
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
