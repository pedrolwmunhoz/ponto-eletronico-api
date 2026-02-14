package com.pontoeletronico.api.exception;

import org.springframework.http.HttpStatus;

public class RegistroPontoInvalidoException extends ApiException {

    public RegistroPontoInvalidoException(String mensagem) {
        super(mensagem != null ? mensagem : "Registro de ponto inválido", HttpStatus.BAD_REQUEST);
    }
}
