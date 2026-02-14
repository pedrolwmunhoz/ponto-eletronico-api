package com.pontoeletronico.api.infrastructure.input.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Doc id 22: Adicionar novo telefone - Request. */
public record UsuarioTelefoneAdicionarRequest(
        @NotBlank(message = "Código do país é obrigatório")
        @Size(max = 10)
        String codigoPais,

        @NotBlank(message = "DDD é obrigatório")
        @Size(max = 5)
        String ddd,

        @NotBlank(message = "Número é obrigatório")
        @Size(max = 20)
        String numero
) {}
