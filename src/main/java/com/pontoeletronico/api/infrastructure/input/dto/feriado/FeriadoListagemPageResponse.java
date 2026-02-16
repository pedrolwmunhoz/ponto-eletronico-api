package com.pontoeletronico.api.infrastructure.input.dto.feriado;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Listar feriados da empresa - Response paginada. */
public record FeriadoListagemPageResponse(
        Paginacao paginacao,
        List<FeriadoItemResponse> conteudo
) {}
