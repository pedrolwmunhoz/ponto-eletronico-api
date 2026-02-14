package com.pontoeletronico.api.infrastructure.input.dto.registro;

import java.time.LocalDate;
import java.util.UUID;

/** Doc id 36: Item da listagem de solicitações de ponto. */
public record SolicitacaoPontoItemResponse(
        UUID id,
        String tipo,
        LocalDate data,
        String motivo,
        String nomeFuncionario,
        String status
) {}
