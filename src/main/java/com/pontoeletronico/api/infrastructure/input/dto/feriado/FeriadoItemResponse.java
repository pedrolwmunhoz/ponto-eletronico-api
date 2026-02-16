package com.pontoeletronico.api.infrastructure.input.dto.feriado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Doc id 52: Listar feriados da empresa - Item da listagem. */
public record FeriadoItemResponse(
        UUID id,
        LocalDate data,
        String descricao,
        Integer tipoFeriadoId,
        String tipoFeriadoDescricao,
        Boolean ativo,
        LocalDateTime createdAt
) {}
