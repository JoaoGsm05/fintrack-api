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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, appProperties.expiration());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = (User) userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, appProperties.expiration());
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email;
        try {
            email = jwtService.extractUsername(request.refreshToken());
        } catch (Exception e) {
            throw new InvalidTokenException();
        }

        User user = (User) userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new InvalidTokenException();
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(newAccessToken, newRefreshToken, appProperties.expiration());
    }
}
