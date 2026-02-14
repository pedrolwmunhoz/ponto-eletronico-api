package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.time.LocalDate;

/**
 * Projeção para listagem de férias/afastamentos.
 * Doc: nomeAfastamento, inicio, fim, status. Para empresa: nomeFuncionario.
 */
public interface FeriasAfastamentosListagemProjection {

    String getNomeFuncionario();
    String getNomeAfastamento();
    LocalDate getInicio();
    LocalDate getFim();
    String getStatus();
}
