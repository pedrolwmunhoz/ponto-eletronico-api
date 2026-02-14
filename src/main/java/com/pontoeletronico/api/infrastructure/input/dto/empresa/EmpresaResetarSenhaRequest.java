package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 10: Resetar senha da empresa - Request. */
@Schema(description = "Resetar senha da empresa (senha antiga + nova)")
public record EmpresaResetarSenhaRequest(
        @NotBlank(message = "senhaAntiga é obrigatória")
        @Schema(description = "Senha atual", requiredMode = Schema.RequiredMode.REQUIRED)
        String senhaAntiga,

        @NotBlank(message = "senhaNova é obrigatória")
        @Size(min = 6, message = "senhaNova deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senhaNova deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Nova senha", requiredMode = Schema.RequiredMode.REQUIRED)
        String senhaNova
) {}
