package com.pontoeletronico.api.exception;

import org.springframework.http.HttpStatus;

public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(mensagem, HttpStatus.CONFLICT);
    }
}
