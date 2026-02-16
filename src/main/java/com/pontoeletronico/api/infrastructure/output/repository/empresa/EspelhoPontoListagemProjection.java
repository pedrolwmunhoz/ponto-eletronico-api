package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.util.UUID;

public interface EspelhoPontoListagemProjection {

    UUID getUsuarioId();
    String getNomeCompleto();
    String getTotalHorasEsperadas();
    String getTotalHorasTrabalhadas();
    String getTotalHorasTrabalhadasFeriado();
}
