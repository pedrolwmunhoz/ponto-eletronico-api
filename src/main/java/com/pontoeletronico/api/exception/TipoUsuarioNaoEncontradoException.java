package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TipoUsuarioNaoEncontradoException extends ApiException {

    public TipoUsuarioNaoEncontradoException() {
        super(MensagemErro.TIPO_USUARIO_NAO_ENCONTRADO.getMensagem(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
