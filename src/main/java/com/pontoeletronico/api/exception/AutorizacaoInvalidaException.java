package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class AutorizacaoInvalidaException extends ApiException {

    public AutorizacaoInvalidaException() {
        super(MensagemErro.AUTORIZACAO_INVALIDA.getMensagem(), HttpStatus.UNAUTHORIZED);
    }
}
