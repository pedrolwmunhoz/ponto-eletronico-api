package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request para atualizar apenas a configuração de banco de horas da empresa")
public record EmpresaAtualizarBancoHorasConfigRequest(
        @NotNull(message = "empresaBancoHorasConfig é obrigatório")
        @Valid
        @Schema(description = "Configuração de banco de horas (ativo e total de dias para vencimento)", requiredMode = Schema.RequiredMode.REQUIRED)
        EmpresaBancoHorasConfigRequest empresaBancoHorasConfig
) {}
