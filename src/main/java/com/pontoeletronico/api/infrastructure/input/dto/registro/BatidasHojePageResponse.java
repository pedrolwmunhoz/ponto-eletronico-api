package com.pontoeletronico.api.infrastructure.input.dto.registro;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Listagem paginada de batidas do dia (funcionário). Params: page, size. */
public record BatidasHojePageResponse(
        Paginacao paginacao,
        List<BatidaHojeItemResponse> conteudo
) {}
