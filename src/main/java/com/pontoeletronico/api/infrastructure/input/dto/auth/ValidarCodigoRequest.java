package com.pontoeletronico.api.infrastructure.input.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Doc id 3: Validar código de recuperação - Request. */
public record ValidarCodigoRequest(
        @NotBlank(message = "codigo é obrigatório")
        @Size(min = 6, max = 6, message = "codigo deve ter exatamente 6 caracteres")
        String codigo
) {}
