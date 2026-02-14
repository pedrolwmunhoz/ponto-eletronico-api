package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Doc id 20/21: Adicionar email / Remover email - Request. */
public record UsuarioEmailRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String novoEmail
) {}
