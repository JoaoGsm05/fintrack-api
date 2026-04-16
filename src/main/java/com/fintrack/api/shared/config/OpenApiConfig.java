package com.fintrack.api.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FinTrack API",
                version = "1.0.0",
                description = """
                        REST API para gestao de financas pessoais.

                        **Funcionalidades:**
                        - Autenticacao JWT com refresh token rotation
                        - Contas, categorias e transacoes com soft delete
                        - Orcamentos por categoria e periodo
                        - Transacoes recorrentes com agendamento automatico
                        - Relatorios de despesas por categoria
                        - Exportacao de dados em CSV

                        **Autenticacao:** use `POST /api/auth/login` para obter o `accessToken`,\
                        depois clique em **Authorize** e informe `Bearer <token>`.
                        """,
                contact = @Contact(
                        name = "Joao Guilherme Souza de Mendonca",
                        email = "joaog.mendonca.eng@gmail.com",
                        url = "https://github.com/JoaoGsm05/fintrack-api"
                ),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente local ou Docker Compose")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token JWT obtido via POST /api/auth/login. Formato: Bearer <token>"
)
public class OpenApiConfig {
}
