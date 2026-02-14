package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

/** Doc id 29: Registro manual — funcionário envia horário sem timezone (ex: 2026-02-01T09:12:00). */
public record RegistroPontoManualRequest(
        @NotNull(message = "horario é obrigatório")
        LocalDateTime horario,
        @NotNull(message = "justificativa é obrigatório")
        String justificativa,
        String observacao,
        @Valid
        RegistroMetadadosRequest registroMetadados
) {}
