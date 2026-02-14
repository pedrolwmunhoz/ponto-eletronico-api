package com.pontoeletronico.api.infrastructure.input.dto.registro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Doc id 28/33: Listar informações de ponto (funcionário / empresa) - Response. Itens por jornada (resumo_ponto_dia). */
public record PontoListagemResponse(
            String jornada,
            LocalDate data,
            String diaSemana,
            String status,
            List<MarcacaoResponse> marcacoes,
            String totalHoras

) {
    /** tipo: "ENTRADA" ou "SAIDA" */
    public record MarcacaoResponse(
            UUID registroId,
            LocalDateTime horario,
            String tipo
    ) {}
}
