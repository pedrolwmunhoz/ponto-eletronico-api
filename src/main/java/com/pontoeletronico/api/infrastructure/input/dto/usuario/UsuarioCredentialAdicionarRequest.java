package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Doc id 24: Adicionar novo tipo de login - Request. */
public record UsuarioCredentialAdicionarRequest(
        @NotNull(message = "Tipo de credencial é obrigatório")
        Integer tipoCredencialId,

        @NotBlank(message = "Valor é obrigatório")
        @Size(min = 2, max = 255, message = "valor deve ter entre 2 e 255 caracteres")
        String valor
) {}
