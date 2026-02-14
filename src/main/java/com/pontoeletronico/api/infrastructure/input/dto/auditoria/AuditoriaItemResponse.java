package com.pontoeletronico.api.infrastructure.input.dto.auditoria;

import java.time.Instant;

/** Doc id 49: Item da listagem de log de auditoria. */
public record AuditoriaItemResponse(
        String acao,
        String descricao,
        Instant data,
        String nomeUsuario,
        Boolean sucesso
) {}
