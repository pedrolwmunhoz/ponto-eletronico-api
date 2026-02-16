package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Doc id 20/21: Adicionar email / Remover email - Request. */
public record UsuarioEmailRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "email deve ter no máximo 255 caracteres")
        String novoEmail
) {}
