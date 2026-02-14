package com.pontoeletronico.api.infrastructure.input.dto.empresa;

/** Doc id 43: Resumo banco de horas - Response. Soma do mês atual + soma do histórico. Valores no formato HH:mm. */
public record ResumoBancoHorasResponse(
        String totalHorasVencidas,
        String totalHorasEsperadas,
        String totalHorasTrabalhadas,
        String totalFinalBanco
) {}
