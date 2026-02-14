package com.pontoeletronico.api.exception;

import org.springframework.http.HttpStatus;

public class RegistroNaoEncontradoException extends ApiException {

    public RegistroNaoEncontradoException(String mensagem) {
        super(mensagem != null ? mensagem : "Registro não encontrado", HttpStatus.NOT_FOUND);
    }
}
