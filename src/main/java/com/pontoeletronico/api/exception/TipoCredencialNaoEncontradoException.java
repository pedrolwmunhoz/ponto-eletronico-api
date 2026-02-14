package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TipoCredencialNaoEncontradoException extends ApiException {

    public TipoCredencialNaoEncontradoException() {
        super(MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
