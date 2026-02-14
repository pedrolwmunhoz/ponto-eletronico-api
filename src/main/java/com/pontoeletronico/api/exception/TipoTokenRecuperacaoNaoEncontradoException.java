package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TipoTokenRecuperacaoNaoEncontradoException extends ApiException {

    public TipoTokenRecuperacaoNaoEncontradoException() {
        super(MensagemErro.TIPO_TOKEN_RECUPERACAO_NAO_ENCONTRADO.getMensagem(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
