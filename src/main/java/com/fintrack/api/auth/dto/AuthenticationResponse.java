package com.fintrack.api.auth.dto;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken
) {}
