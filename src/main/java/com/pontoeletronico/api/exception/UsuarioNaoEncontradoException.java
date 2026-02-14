package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class UsuarioNaoEncontradoException extends ApiException {

    public UsuarioNaoEncontradoException() {
        super(MensagemErro.USUARIO_NAO_ENCONTRADO.getMensagem(), HttpStatus.UNAUTHORIZED);
    }
}
