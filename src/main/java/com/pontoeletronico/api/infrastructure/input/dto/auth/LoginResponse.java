package com.pontoeletronico.api.infrastructure.input.dto.auth;

/** Doc id 1: Login de usuário - Response. */
public record LoginResponse(
        String jwt,
        String jwtExpires,
        String refreshToken,
        String refreshTokenExpires
) {}
