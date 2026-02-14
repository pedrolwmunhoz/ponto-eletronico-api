package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TipoNaoEncontradoException extends ApiException {

    public TipoNaoEncontradoException(MensagemErro mensagem) {
        super(mensagem.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
