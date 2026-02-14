package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.util.UUID;

public interface FuncionarioListagemProjection {

    UUID getUsuarioId();
    String getUsername();
    String getTipo();
    String getEmails();
    String getTelefones();
}
