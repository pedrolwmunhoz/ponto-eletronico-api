package com.pontoeletronico.api.infrastructure.input.dto.auditoria;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** Doc id 50: Detalhar log de auditoria - Response. */
public record AuditoriaDetalheResponse(
        UUID usuarioId,
        String acao,
        String descricao,
        Map<String, Object> dadosAntigos,
        Map<String, Object> dadosNovos,
        UUID dispositivoId,
        Boolean sucesso,
        String mensagemErro,
        LocalDateTime createdAt
) {}
