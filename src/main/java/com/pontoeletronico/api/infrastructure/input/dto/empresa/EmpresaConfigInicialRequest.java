package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Doc id 9: Configuração inicial da empresa - Request. */
@Schema(description = "Configuração inicial da empresa (jornada, banco de horas, geofences)")
public record EmpresaConfigInicialRequest(
        @NotNull(message = "empresaJornadaConfig é obrigatório")
        @Valid
        @Schema(description = "Configuração de jornada padrão da empresa", requiredMode = Schema.RequiredMode.REQUIRED)
        EmpresaJornadaConfigRequest empresaJornadaConfig,

        @NotNull(message = "empresaBancoHorasConfig é obrigatório")
        @Valid
        @Schema(description = "Configuração de banco de horas da empresa", requiredMode = Schema.RequiredMode.REQUIRED)
        EmpresaBancoHorasConfigRequest empresaBancoHorasConfig,

        @Schema(description = "Lista opcional de geofences da empresa (pode ser vazia)")
        List<@Valid UsuarioGeofenceRequest> usuarioGeofence
) {}
