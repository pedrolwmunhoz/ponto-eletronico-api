package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import java.time.Duration;

public interface EmpresaPerfilProjection {

    String getUsername();
    String getCnpj();
    String getRazaoSocial();
    String getEmail();
    java.util.UUID getTelefoneId();
    String getCodigoPais();
    String getDdd();
    String getNumero();
    String getRua();
    String getNumeroEndereco();
    String getComplemento();
    String getBairro();
    String getCidade();
    String getUf();
    String getCep();
    String getTimezone();
    Duration getCargaDiariaPadrao();
    Duration getCargaSemanalPadrao();
    Duration getToleranciaPadrao();
    Duration getIntervaloPadrao();
    Boolean getControlePontoObrigatorio();
    String getTipoModeloPonto();
    Integer getTempoRetencao();
    Boolean getAuditoriaAtiva();
    Boolean getAssinaturaDigitalObrigatoria();
    Boolean getGravarGeolocalizacaoObrigatoria();
    Boolean getPermitirAjustePontoDireto();
}
