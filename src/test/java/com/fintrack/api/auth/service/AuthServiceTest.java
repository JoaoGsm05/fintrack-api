package com.fintrack.api.auth.service;

import com.fintrack.api.auth.dto.AuthResponse;
import com.fintrack.api.auth.dto.LoginRequest;
import com.fintrack.api.auth.dto.RefreshRequest;
import com.fintrack.api.auth.dto.RegisterRequest;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.shared.config.AppProperties;
import com.fintrack.api.shared.exception.EmailAlreadyExistsException;
import com.fintrack.api.shared.exception.InvalidCredentialsException;
import com.fintrack.api.shared.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    private User fakeUser;

    @BeforeEach
    void setUp() {
        fakeUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Throws EmailAlreadyExistsException when email is already registered")
        void register_emailAlreadyExists_throwsEmailAlreadyExistsException() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Saves user and returns AuthResponse with Bearer token type when email is new")
        void register_newEmail_savesUserAndReturnsAuthResponse() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
            when(appProperties.expiration()).thenReturn(900_000L);

            RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

            AuthResponse response = authService.register(request);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Calls passwordEncoder.encode() with the raw password")
        void register_newEmail_encodesPasswordWithRawValue() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
            when(appProperties.expiration()).thenReturn(900_000L);

            authService.register(new RegisterRequest("Test User", "test@example.com", "password123"));

            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Calls userRepository.save() exactly once")
        void register_newEmail_savesUserExactlyOnce() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
            when(appProperties.expiration()).thenReturn(900_000L);

            authService.register(new RegisterRequest("Test User", "test@example.com", "password123"));

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Returns AuthResponse when credentials are valid")
        void login_validCredentials_returnsAuthResponse() {
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(fakeUser);
            when(jwtService.generateAccessToken(fakeUser)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(fakeUser)).thenReturn("refresh-token");
            when(appProperties.expiration()).thenReturn(900_000L);

            AuthResponse response = authService.login(new LoginRequest("test@example.com", "password123"));

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Throws InvalidCredentialsException when AuthenticationManager raises BadCredentialsException")
        void login_badCredentials_throwsInvalidCredentialsException() {
            doThrow(new BadCredentialsException("bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong-pass")))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // refresh()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("Returns new AuthResponse when refresh token is valid")
        void refresh_validToken_returnsNewAuthResponse() {
            when(jwtService.extractUsername("valid-refresh-token")).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(fakeUser);
            when(jwtService.isTokenValid("valid-refresh-token", fakeUser)).thenReturn(true);
            when(jwtService.generateAccessToken(fakeUser)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(fakeUser)).thenReturn("new-refresh-token");
            when(appProperties.expiration()).thenReturn(900_000L);

            AuthResponse response = authService.refresh(new RefreshRequest("valid-refresh-token"));

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Throws InvalidTokenException when JwtService throws exception on extractUsername")
        void refresh_jwtServiceThrows_throwsInvalidTokenException() {
            when(jwtService.extractUsername("bad-token"))
                    .thenThrow(new RuntimeException("JWT parse error"));

            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("bad-token")))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Throws InvalidTokenException when isTokenValid returns false")
        void refresh_tokenNotValid_throwsInvalidTokenException() {
            when(jwtService.extractUsername("expired-token")).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(fakeUser);
            when(jwtService.isTokenValid("expired-token", fakeUser)).thenReturn(false);

            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("expired-token")))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }
}
