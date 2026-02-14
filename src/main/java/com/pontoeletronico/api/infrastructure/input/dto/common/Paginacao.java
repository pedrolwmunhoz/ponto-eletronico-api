package com.pontoeletronico.api.infrastructure.input.dto.common;

public record Paginacao(
        int totalPaginas,
        long totalElementos,
        int totalElementosPaginaAtual,
        int paginaAtual
) {}
