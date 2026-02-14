package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TelefoneNaoEncontradoException extends ApiException {

    public TelefoneNaoEncontradoException() {
        super(MensagemErro.TELEFONE_NAO_ENCONTRADO.getMensagem(), HttpStatus.NOT_FOUND);
    }
}
