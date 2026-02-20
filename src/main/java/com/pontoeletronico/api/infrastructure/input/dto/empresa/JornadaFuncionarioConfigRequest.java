package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.Duration;
import java.time.LocalTime;

@Schema(description = "Configuração de jornada do funcionário")
public record JornadaFuncionarioConfigRequest(
        @NotNull(message = "tipoEscalaJornadaId é obrigatório")
        @Schema(description = "ID do tipo de escala jornada (FK tipo_escala_jornada)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer tipoEscalaJornadaId,
        
        @NotNull(message = "cargaHorariaDiaria é obrigatória")
        @DurationMaxHours(value = 12, message = "cargaHorariaDiaria deve ser no máximo 12 horas")
        @Schema(description = "Carga horária diária (ISO-8601 Duration, ex: PT8H)", example = "PT8H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration cargaHorariaDiaria,
        
        @NotNull(message = "cargaHorariaSemanal é obrigatória")
        @DurationMaxHours(value = 44, message = "cargaHorariaSemanal deve ser no máximo 44 horas")
        @Schema(description = "Carga horária semanal (ISO-8601 Duration, ex: PT44H)", example = "PT44H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration cargaHorariaSemanal,
        
        @NotNull(message = "entradaPadrao é obrigatória")
        @Schema(description = "Horário de entrada padrão (HH:mm)", example = "08:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalTime entradaPadrao,
        
        @NotNull(message = "saidaPadrao é obrigatória")
        @Schema(description = "Horário de saída padrão (HH:mm)", example = "17:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalTime saidaPadrao,
        
        @DurationMaxHours(value = 6, message = "toleranciaPadrao deve ser no máximo 6 horas")
        @Schema(description = "Tolerância padrão (ISO-8601 Duration, ex: PT0S)", example = "PT0S")
        Duration toleranciaPadrao,
        
        @NotNull(message = "intervaloPadrao é obrigatório")
        @DurationMaxHours(value = 6, message = "intervaloPadrao deve ser no máximo 6 horas")
        @Schema(description = "Intervalo padrão (ISO-8601 Duration, ex: PT1H)", example = "PT1H", requiredMode = Schema.RequiredMode.REQUIRED)
        Duration intervaloPadrao,

        @Schema(description = "Tempo descanso entre jornadas (ISO-8601 Duration, ex: PT11H - CLT art. 66)", example = "PT11H")
        Duration tempoDescansoEntreJornada,
        
        @Schema(description = "Gravação de geolocalização obrigatória", example = "false")
        boolean gravaGeoObrigatoria
) {}
