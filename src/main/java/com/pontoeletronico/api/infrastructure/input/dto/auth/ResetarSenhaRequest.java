package com.pontoeletronico.api.infrastructure.input.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 4: Resetar senha com token temporário - Request. */
public record ResetarSenhaRequest(
        @NotBlank(message = "token é obrigatório")
        String token,
        @NotBlank(message = "senhaNova é obrigatória")
        @Size(min = 6, message = "senhaNova deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senhaNova deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        String senhaNova
) {}
