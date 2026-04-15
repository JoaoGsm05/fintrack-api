package com.fintrack.api.shared.exception;

import com.fintrack.api.auth.controller.AuthController;
import com.fintrack.api.auth.dto.LoginRequest;
import com.fintrack.api.auth.dto.RefreshRequest;
import com.fintrack.api.auth.dto.RegisterRequest;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // Beans requeridos pelo SecurityConfig no slice @WebMvcTest
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    // ─────────────────────────────────────────────────────────────────────────
    // ProblemDetail format (RFC 7807)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ProblemDetail response format (RFC 7807)")
    class ProblemDetailFormat {

        @Test
        @DisplayName("Error response contains 'title' field")
        void errorResponse_hasTitle() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test User",
                                        "email": "taken@example.com",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(jsonPath("$.title").exists());
        }

        @Test
        @DisplayName("Error response contains 'detail' field")
        void errorResponse_hasDetail() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test User",
                                        "email": "taken@example.com",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("Error response contains 'status' field")
        void errorResponse_hasStatus() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test User",
                                        "email": "taken@example.com",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(jsonPath("$.status").exists());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status codes per exception type
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exception → HTTP status mapping")
    class StatusMapping {

        @Test
        @DisplayName("EmailAlreadyExistsException → 409 Conflict")
        void emailAlreadyExists_returns409() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test User",
                                        "email": "taken@example.com",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("InvalidCredentialsException → 401 Unauthorized")
        void invalidCredentials_returns401() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new InvalidCredentialsException());

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "user@example.com",
                                        "password": "wrongpassword"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("InvalidTokenException → 401 Unauthorized")
        void invalidToken_returns401() throws Exception {
            when(authService.refresh(any(RefreshRequest.class)))
                    .thenThrow(new InvalidTokenException());

            mockMvc.perform(post("/api/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "refreshToken": "some-token"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Validation errors → 422 Unprocessable Entity with 'errors' field")
        void validationError_returns422WithErrorsField() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "",
                                        "email": "not-an-email",
                                        "password": "short"
                                    }
                                    """))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors").exists());
        }
    }
}
