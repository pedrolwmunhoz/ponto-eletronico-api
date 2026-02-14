package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class CredencialNaoEncontradaException extends ApiException {

    public CredencialNaoEncontradaException() {
        super(MensagemErro.CREDENCIAL_NAO_ENCONTRADA.getMensagem(), HttpStatus.NOT_FOUND);
    }
}
