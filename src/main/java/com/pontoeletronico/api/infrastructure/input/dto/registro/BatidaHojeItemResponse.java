package com.pontoeletronico.api.infrastructure.input.dto.registro;

import java.time.LocalDateTime;
import java.util.UUID;

/** Item de batida do dia (registro de ponto) para listagem paginada. */
public record BatidaHojeItemResponse(
        UUID registroId,
        LocalDateTime horario,
        String tipo
) {}
