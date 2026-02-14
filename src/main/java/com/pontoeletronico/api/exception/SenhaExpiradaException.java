package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class SenhaExpiradaException extends ApiException {

    public SenhaExpiradaException() {
        super(MensagemErro.SENHA_EXPIRADA.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
