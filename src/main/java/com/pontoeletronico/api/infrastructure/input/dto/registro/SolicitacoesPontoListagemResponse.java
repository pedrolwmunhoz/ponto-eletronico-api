package com.pontoeletronico.api.infrastructure.input.dto.registro;

import java.util.List;

/** Doc id 36: Listar solicitações de ponto - Response. */
public record SolicitacoesPontoListagemResponse(
        List<SolicitacaoPontoItemResponse> items,
        long total,
        int page,
        int size
) {}
