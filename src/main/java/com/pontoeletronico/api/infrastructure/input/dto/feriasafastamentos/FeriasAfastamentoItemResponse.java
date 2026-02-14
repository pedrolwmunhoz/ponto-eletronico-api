package com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos;

import java.time.LocalDate;

/** Doc id 39/40/41: Item da listagem de férias e afastamentos. */
public record FeriasAfastamentoItemResponse(
        String nomeFuncionario,
        String nomeAfastamento,
        LocalDate inicio,
        LocalDate fim,
        String status
) {
    /** Para listagem do funcionário (sem nomeFuncionario). */
    public static FeriasAfastamentoItemResponse semNomeFuncionario(String nomeAfastamento, LocalDate inicio, LocalDate fim, String status) {
        return new FeriasAfastamentoItemResponse(null, nomeAfastamento, inicio, fim, status);
    }

    /** Para listagem da empresa (com nomeFuncionario). */
    public static FeriasAfastamentoItemResponse comNomeFuncionario(String nomeFuncionario, String nomeAfastamento, LocalDate inicio, LocalDate fim, String status) {
        return new FeriasAfastamentoItemResponse(nomeFuncionario, nomeAfastamento, inicio, fim, status);
    }
}
