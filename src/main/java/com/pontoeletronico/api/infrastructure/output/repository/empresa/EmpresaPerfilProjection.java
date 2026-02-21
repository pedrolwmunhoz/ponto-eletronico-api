package com.pontoeletronico.api.infrastructure.output.repository.empresa;

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
    /** Valor no banco: ISO-8601 (PT8H) ou numérico em nanossegundos (legado). */
    String getCargaDiariaPadrao();
    String getCargaSemanalPadrao();
    String getToleranciaPadrao();
    String getIntervaloPadrao();
    Boolean getControlePontoObrigatorio();
    String getTipoModeloPonto();
    Integer getTempoRetencao();
    Boolean getAuditoriaAtiva();
    Boolean getAssinaturaDigitalObrigatoria();
    Boolean getGravarGeolocalizacaoObrigatoria();
    Boolean getPermitirAjustePontoDireto();
    java.time.LocalDateTime getDataExpiracaoCertificado();
}
