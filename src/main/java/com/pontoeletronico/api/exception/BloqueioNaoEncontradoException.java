package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class BloqueioNaoEncontradoException extends ApiException {

    public BloqueioNaoEncontradoException() {
        super(MensagemErro.BLOQUEIO_NAO_ENCONTRADO.getMensagem(), HttpStatus.NOT_FOUND);
    }
}
