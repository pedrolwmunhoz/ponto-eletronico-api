package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 13: Resetar senha de funcionário - Request. */
@Schema(description = "Requisição para resetar senha do funcionário")
public record FuncionarioResetarSenhaRequest(
        @NotBlank(message = "senhaNova é obrigatória")
        @Size(min = 6, message = "senhaNova deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senhaNova deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Nova senha", example = "NovaSenha123!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senhaNova
) {}
