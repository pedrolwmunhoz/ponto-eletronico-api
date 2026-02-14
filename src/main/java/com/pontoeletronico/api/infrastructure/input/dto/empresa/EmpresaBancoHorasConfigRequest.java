package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Configuração de banco de horas da empresa")
public record EmpresaBancoHorasConfigRequest(
        @NotNull(message = "ativo é obrigatório")
        @Schema(description = "Banco de horas ativo", example = "false")
        boolean ativo,

        @NotNull(message = "totalDiasVencimento é obrigatório")
        @Positive(message = "totalDiasVencimento deve ser positivo")
        @Schema(description = "Total de dias para vencimento do banco de horas", example = "365", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer totalDiasVencimento
) {}
