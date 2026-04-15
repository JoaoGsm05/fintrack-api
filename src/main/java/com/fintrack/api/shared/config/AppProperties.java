package com.fintrack.api.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record AppProperties(
        String secret,
        long expiration,
        long refreshExpiration
) {}
