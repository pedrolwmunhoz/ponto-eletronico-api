package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class EmpresaNaoEncontradaException extends ApiException {

    public EmpresaNaoEncontradaException() {
        super(MensagemErro.EMPRESA_NAO_ENCONTRADA.getMensagem(), HttpStatus.NOT_FOUND);
    }
}
