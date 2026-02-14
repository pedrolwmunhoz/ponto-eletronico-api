package com.pontoeletronico.api.infrastructure.output.repository.usuario;

import java.util.UUID;

public interface UsuarioListagemProjection {

    UUID getUsuarioId();
    String getUsername();
    String getTipo();
    String getEmails();
    String getTelefones();
}
