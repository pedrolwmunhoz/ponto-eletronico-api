package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String mensagem) {
        super(mensagem != null ? mensagem : MensagemErro.BAD_REQUEST.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
