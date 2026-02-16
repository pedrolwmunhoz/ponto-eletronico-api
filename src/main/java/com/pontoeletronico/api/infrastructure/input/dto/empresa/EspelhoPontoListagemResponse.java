package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import java.util.UUID;

/** Item da listagem espelho de ponto (empresa). Nome e todos os totais mensais. */
public record EspelhoPontoListagemResponse(
        UUID usuarioId,
        String nomeCompleto,
        String totalHorasEsperadas,
        String totalHorasTrabalhadas,
        String totalHorasTrabalhadasFeriado
) {}
