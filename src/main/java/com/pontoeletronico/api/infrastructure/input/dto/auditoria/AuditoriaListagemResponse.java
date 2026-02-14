package com.pontoeletronico.api.infrastructure.input.dto.auditoria;

import java.util.List;

/** Doc id 49: Listar log de auditoria - Response. */
public record AuditoriaListagemResponse(
        List<AuditoriaItemResponse> items,
        long total,
        int page,
        int size
) {}
