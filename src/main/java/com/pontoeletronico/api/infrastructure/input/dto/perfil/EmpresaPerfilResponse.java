package com.pontoeletronico.api.infrastructure.input.dto.perfil;

import java.time.Duration;

/** Doc id 27: Recuperar informações da empresa - Response. */
public record EmpresaPerfilResponse(
        String username,
        String cnpj,
        String razaoSocial,
        String email,
        String codigoPais,
        String ddd,
        String numero,
        String rua,
        String numeroEndereco,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        String timezone,
        Duration cargaDiariaPadrao,
        Duration cargaSemanalPadrao,
        Duration toleranciaPadrao,
        Duration intervaloPadrao,
        Boolean controlePontoObrigatorio,
        String tipoModeloPonto,
        Integer tempoRetencao,
        Boolean auditoriaAtiva,
        Boolean assinaturaDigitalObrigatoria,
        Boolean gravarGeolocalizacaoObrigatoria,
        Boolean permitirAjustePontoDireto
) {
}
