package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Doc id 19: Atualizar username - Request. */
public record UsuarioPerfilAtualizarRequest(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 2, max = 255, message = "username deve ter entre 2 e 255 caracteres")
        String username
) {}
