package com.fintrack.api.auth.security;

import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.shared.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // Secret deve ter pelo menos 256 bits (32 bytes) para HMAC-SHA256
    private static final String SECRET = "test-secret-key-for-unit-tests-only-256bits";
    private static final long EXPIRATION = 900_000L;              // 15 min em ms
    private static final long REFRESH_EXPIRATION = 604_800_000L;  // 7 dias em ms

    private JwtService jwtService;
    private User fakeUser;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(SECRET, EXPIRATION, REFRESH_EXPIRATION);
        jwtService = new JwtService(properties);

        fakeUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateAccessToken
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessToken {

        @Test
        @DisplayName("Returns a non-null, non-blank token string")
        void generateAccessToken_validUser_returnsNonBlankToken() {
            String token = jwtService.generateAccessToken(fakeUser);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Returned token encodes the user email as subject")
        void generateAccessToken_validUser_subjectIsUserEmail() {
            String token = jwtService.generateAccessToken(fakeUser);

            String extracted = jwtService.extractUsername(token);

            assertThat(extracted).isEqualTo(fakeUser.getUsername());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateRefreshToken
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshToken {

        @Test
        @DisplayName("Returns a token different from the access token")
        void generateRefreshToken_validUser_differentFromAccessToken() {
            String accessToken = jwtService.generateAccessToken(fakeUser);
            String refreshToken = jwtService.generateRefreshToken(fakeUser);

            assertThat(refreshToken).isNotEqualTo(accessToken);
        }

        @Test
        @DisplayName("Refresh token encodes the user email as subject")
        void generateRefreshToken_validUser_subjectIsUserEmail() {
            String refreshToken = jwtService.generateRefreshToken(fakeUser);

            String extracted = jwtService.extractUsername(refreshToken);

            assertThat(extracted).isEqualTo(fakeUser.getUsername());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isTokenValid
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("Returns true for a freshly generated access token")
        void isTokenValid_freshToken_returnsTrue() {
            String token = jwtService.generateAccessToken(fakeUser);

            assertThat(jwtService.isTokenValid(token, fakeUser)).isTrue();
        }

        @Test
        @DisplayName("Returns false when token was signed with a different secret")
        void isTokenValid_differentSecret_returnsFalse() {
            AppProperties otherProps = new AppProperties(
                    "other-completely-different-secret-key-256bits!!", EXPIRATION, REFRESH_EXPIRATION);
            JwtService otherService = new JwtService(otherProps);

            String tokenFromOtherService = otherService.generateAccessToken(fakeUser);

            // Validar com o serviço original — secret diferente → deve retornar false
            assertThat(jwtService.isTokenValid(tokenFromOtherService, fakeUser)).isFalse();
        }

        @Test
        @DisplayName("Returns false for an expired token (1 ms expiration)")
        void isTokenValid_expiredToken_returnsFalse() throws InterruptedException {
            AppProperties shortLived = new AppProperties(SECRET, 1L, 1L);
            JwtService shortService = new JwtService(shortLived);

            String token = shortService.generateAccessToken(fakeUser);

            // Mínimo necessário para garantir a expiração
            Thread.sleep(2);

            assertThat(shortService.isTokenValid(token, fakeUser)).isFalse();
        }

        @Test
        @DisplayName("Returns false when token subject belongs to a different user")
        void isTokenValid_differentUser_returnsFalse() {
            User anotherUser = User.builder()
                    .name("Another User")
                    .email("another@example.com")
                    .passwordHash("hashed-password")
                    .role(Role.USER)
                    .build();

            String token = jwtService.generateAccessToken(fakeUser);

            // Subject é fakeUser.email, validando contra anotherUser → false
            assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // extractUsername
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername {

        @Test
        @DisplayName("Returns the correct email from an access token")
        void extractUsername_accessToken_returnsCorrectEmail() {
            String token = jwtService.generateAccessToken(fakeUser);

            assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Returns the correct email from a refresh token")
        void extractUsername_refreshToken_returnsCorrectEmail() {
            String token = jwtService.generateRefreshToken(fakeUser);

            assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        }
    }
}
