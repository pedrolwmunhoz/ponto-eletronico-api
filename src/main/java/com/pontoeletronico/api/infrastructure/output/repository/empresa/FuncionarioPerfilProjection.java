package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface FuncionarioPerfilProjection {

    String getUsername();
    Boolean getFuncionarioAtivo();
    String getNomeCompleto();
    String getCpf();
    LocalDate getDataNascimento();
    String getMatricula();
    String getEmail();
    String getCodigoPais();
    String getDdd();
    String getNumero();
    Boolean getContratoAtivo();
    String getCargo();
    String getDepartamento();
    String getTipoContrato();
    LocalDate getDataAdmissao();
    LocalDate getDataDemissao();
    BigDecimal getSalarioMensal();
    BigDecimal getSalarioHora();
    String getTipoEscala();
    String getCargaHorariaDiaria();
    String getCargaHorariaSemanal();
    LocalTime getEntradaPadrao();
    LocalTime getSaidaPadrao();
    String getToleranciaPadrao();
    String getIntervaloPadrao();
}
