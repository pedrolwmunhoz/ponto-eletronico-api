package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class CredencialInvalidaException extends ApiException {

    public CredencialInvalidaException() {
        super(MensagemErro.CREDENCIAL_INVALIDA.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
