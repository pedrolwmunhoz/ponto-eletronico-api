package com.pontoeletronico.api.infrastructure.output.repository.registro;

import java.time.LocalDate;
import java.util.UUID;

public interface SolicitacaoPontoListagemProjection {

    UUID getId();
    String getTipo();
    LocalDate getData();
    String getMotivo();
    String getNomeFuncionario();
    String getStatus();
}
