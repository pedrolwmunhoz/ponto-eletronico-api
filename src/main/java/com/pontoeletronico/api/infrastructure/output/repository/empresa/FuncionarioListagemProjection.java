package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.util.UUID;

public interface FuncionarioListagemProjection {

    UUID getUsuarioId();
    String getNomeCompleto();
    String getPrimeiroNome();
    String getUltimoNome();
    String getUsername();
    String getEmails();
    String getTelefones();
}
