package com.pontoeletronico.api.infrastructure.input.dto.relatorio;

import java.util.List;

/** Doc id 49: Estrutura do relatório de ponto resumo. Dados do fechamento (banco_horas_historico). */
public record RelatorioPontoResumoDto(
        String periodo,
        List<FuncionarioResumoDto> lista
) {
    public record FuncionarioResumoDto(
            String funcionarioId,
            String nome,
            String totalHorasEsperadas,
            String totalHorasTrabalhadas,
            String totalBancoHorasFinal,
            String status
    ) {}
}
