package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 14: Resetar email de funcionário - Request. */
@Schema(description = "Requisição para resetar email do funcionário")
public record FuncionarioResetarEmailRequest(
        @NotBlank(message = "emailNovo é obrigatório")
        @Size(max = 255, message = "emailNovo deve ter no máximo 255 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "email inválido")
        @Schema(description = "Novo email", example = "novo@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String emailNovo
) {}
