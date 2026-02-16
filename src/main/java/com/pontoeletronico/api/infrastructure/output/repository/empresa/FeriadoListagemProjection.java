package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Projeção para listagem de feriados da empresa. */
public interface FeriadoListagemProjection {

    UUID getId();
    LocalDate getData();
    String getDescricao();
    Integer getTipoFeriadoId();
    String getTipoFeriadoDescricao();
    Boolean getAtivo();
    LocalDateTime getCreatedAt();
}
