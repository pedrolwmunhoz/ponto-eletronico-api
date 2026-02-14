package com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos;

import java.util.List;

/** Doc id 39/40/41: Listar férias e afastamentos (funcionário / por id / empresa) - Response. */
public record FeriasAfastamentosListagemResponse(
        List<FeriasAfastamentoItemResponse> items,
        long total,
        int page,
        int size
) {}
