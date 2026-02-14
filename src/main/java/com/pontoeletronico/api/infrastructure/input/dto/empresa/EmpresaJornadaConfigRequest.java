package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.Duration;
import java.time.LocalTime;

/** Doc id 11: Atualizar jornada padrão da empresa - Request. */
@Schema(description = "Configuração de jornada padrão da empresa")
public record EmpresaJornadaConfigRequest(
        @NotNull(message = "tipoEscalaJornadaId é obrigatório")
        @Schema(description = "ID do tipo de escala jornada (FK tipo_escala_jornada)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer tipoEscalaJornadaId,

        @NotNull(message = "cargaHorariaDiaria é obrigatória")
        @Schema(description = "Carga horária diária (ISO-8601 Duration, ex: PT8H)", example = "PT8H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration cargaHorariaDiaria,

        @NotNull(message = "cargaHorariaSemanal é obrigatória")
        @Schema(description = "Carga horária semanal (ISO-8601 Duration, ex: PT44H)", example = "PT44H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration cargaHorariaSemanal,

        @NotNull(message = "toleranciaPadrao é obrigatório")
        @Schema(description = "Tolerância padrão (ISO-8601 Duration, ex: PT0S)", example = "PT0S")
        Duration toleranciaPadrao,

        @NotNull(message = "intervaloPadrao é obrigatório")
        @Schema(description = "Intervalo padrão (ISO-8601 Duration, ex: PT1H)", example = "PT1H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration intervaloPadrao,

        @Schema(description = "Tempo descanso entre jornadas (ISO-8601 Duration, ex: PT11H - CLT art. 66)", example = "PT11H")
        Duration tempoDescansoEntreJornada,

        @NotNull(message = "entradaPadrao é obrigatória")
        @Schema(description = "Horário de entrada padrão (HH:mm)", example = "08:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalTime entradaPadrao,

        @NotNull(message = "saidaPadrao é obrigatória")
        @Schema(description = "Horário de saída padrão (HH:mm)", example = "17:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalTime saidaPadrao,

        @NotBlank(message = "timezone é obrigatório")
        @Size(max = 50)
        @Schema(description = "Timezone (default: America/Sao_Paulo)", example = "America/Sao_Paulo")
        String timezone,

        @Schema(description = "Gravação de geolocalização obrigatória", example = "false")
        boolean gravaGeoObrigatoria,

        @Schema(description = "Gravar ponto apenas em geofence", example = "false")
        boolean gravaPontoApenasEmGeofence,

        @Schema(description = "Permitir ajuste de ponto", example = "false")
        boolean permiteAjustePonto
) {}
