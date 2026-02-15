package com.pontoeletronico.api.infrastructure.output.repository.audit;

import java.time.LocalDateTime;

/**
 * Projeção para listagem de auditoria. Doc: acao, descricao, data, nomeUsuario, sucesso
 */
public interface AuditoriaLogListagemProjection {

    String getAcao();
    String getDescricao();
    LocalDateTime getData();
    String getNomeUsuario();
    Boolean getSucesso();
}
