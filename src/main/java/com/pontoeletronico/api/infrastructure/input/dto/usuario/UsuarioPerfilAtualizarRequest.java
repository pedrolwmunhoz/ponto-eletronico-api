package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Doc id 19: Atualizar username - Request. */
public record UsuarioPerfilAtualizarRequest(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 1, max = 255)
        String username
) {}
