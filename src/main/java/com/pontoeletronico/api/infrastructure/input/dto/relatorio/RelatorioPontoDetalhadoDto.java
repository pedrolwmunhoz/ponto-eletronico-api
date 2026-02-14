package com.pontoeletronico.api.infrastructure.input.dto.relatorio;

import java.time.LocalDate;
import java.util.List;

/** Doc id 47: Estrutura de dados do relatório de ponto detalhado (por funcionário e dia). */
public record RelatorioPontoDetalhadoDto(
        PeriodoDto periodo,
        List<FuncionarioDetalhadoDto> funcionarios
) {
    public record PeriodoDto(String inicio, String fim) {}

    public record FuncionarioDetalhadoDto(
            String funcionarioId,
            String nome,
            String jornadaPrevistaDia,
            List<RegistroDiaDto> registros
    ) {}

    public record RegistroDiaDto(
            LocalDate data,
            String diaSemana,
            String entrada1,
            String saida1,
            String entrada2,
            String saida2,
            String horasDia,
            String extrasDia,
            String faltaDia,
            String ocorrencia
    ) {}
}
