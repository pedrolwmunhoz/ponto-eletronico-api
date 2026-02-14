package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import java.time.LocalDateTime;
import java.util.UUID;

/** Item do histórico de banco de horas (fechamento mensal). */
public record BancoHorasHistoricoResponse(
        UUID id,
        UUID funcionarioId,
        Integer anoReferencia,
        Integer mesReferencia,
        String totalHorasEsperadas,
        String totalHorasTrabalhadas,
        String totalBancoHorasFinal,
        String status,
        Integer valorCompensadoParcial,
        String statusPagamento,
        Boolean ativo,
        LocalDateTime dataDesativacao
) {}
