package com.pontoeletronico.api.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final String mensagem;
    private final HttpStatus status;

    public ApiException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.mensagem = mensagem;
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
