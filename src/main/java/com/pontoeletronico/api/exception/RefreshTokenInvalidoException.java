package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class RefreshTokenInvalidoException extends ApiException {

    public RefreshTokenInvalidoException() {
        super(MensagemErro.REFRESH_TOKEN_INVALIDO.getMensagem(), HttpStatus.UNAUTHORIZED);
    }
}
