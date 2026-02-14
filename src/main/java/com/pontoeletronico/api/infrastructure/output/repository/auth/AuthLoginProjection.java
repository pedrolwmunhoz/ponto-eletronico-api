package com.pontoeletronico.api.infrastructure.output.repository.auth;

import java.util.UUID;

public interface AuthLoginProjection {

    UUID getUsuarioId();
    String getUsername();
    String getSenhaHash();
    UUID getCredencialId();
    Boolean getAtivo();
    Boolean getBloqueio();
    Boolean getSenhaExpirada();
    String getTipoDescricao();
}
