package com.pontoeletronico.api.infrastructure.output.repository.audit;

import java.time.Instant;

/**
 * Projeção para listagem de auditoria. Doc: acao, descricao, data, nomeUsuario, sucesso
 */
public interface AuditoriaLogListagemProjection {

    String getAcao();
    String getDescricao();
    Instant getData();
    String getNomeUsuario();
    Boolean getSucesso();
}
