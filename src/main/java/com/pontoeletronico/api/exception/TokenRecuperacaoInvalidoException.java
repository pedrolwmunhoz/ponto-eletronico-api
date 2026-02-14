package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TokenRecuperacaoInvalidoException extends ApiException {

    public TokenRecuperacaoInvalidoException() {
        super(MensagemErro.TOKEN_RECUPERACAO_INVALIDO.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
