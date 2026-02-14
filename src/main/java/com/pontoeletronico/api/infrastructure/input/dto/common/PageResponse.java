package com.pontoeletronico.api.infrastructure.input.dto.common;

import java.util.List;

public record PageResponse<T>(
        int totalPaginas,
        long totalElementos,
        int totalElementosPaginaAtual,
        int paginaAtual,
        List<T> conteudo
) {}
