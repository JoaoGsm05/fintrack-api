package com.fintrack.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Payload para autenticacao do usuario")
public record LoginRequest(
        @Schema(description = "E-mail cadastrado", example = "joao@fintrack.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Senha do usuario", example = "SenhaForte123")
        @NotBlank(message = "Password is required")
        String password
) {}
