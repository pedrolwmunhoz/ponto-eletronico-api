package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Listagem espelho de ponto - Response paginada. */
public record EspelhoPontoListagemPageResponse(
        Paginacao paginacao,
        List<EspelhoPontoListagemResponse> conteudo
) {}
