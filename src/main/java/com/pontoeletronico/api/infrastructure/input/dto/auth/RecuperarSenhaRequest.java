package com.pontoeletronico.api.infrastructure.input.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Doc id 2: Gerar código de recuperação de senha - Request. */
public record RecuperarSenhaRequest(
        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        String email
) {}
