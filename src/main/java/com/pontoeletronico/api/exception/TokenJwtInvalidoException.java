package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TokenJwtInvalidoException extends ApiException {

    public TokenJwtInvalidoException() {
        super(MensagemErro.TOKEN_JWT_INVALIDO.getMensagem(), HttpStatus.UNAUTHORIZED);
    }
}
