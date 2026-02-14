package com.pontoeletronico.api.infrastructure.input.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Doc id 5: Refresh token - Request. */
public record RefreshRequest(
        @NotBlank(message = "refreshToken é obrigatório")
        String refreshToken
) {}
