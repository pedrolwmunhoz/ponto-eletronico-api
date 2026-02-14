package com.pontoeletronico.api.infrastructure.input.dto.auth;

/** Doc id 5: Refresh token - Response. */
public record RefreshResponse(
        String token,
        String expiresToken,
        String refreshToken,
        String expiresRefreshToken
) {}
