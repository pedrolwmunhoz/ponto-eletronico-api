package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class UsuarioBloqueadoException extends ApiException {

    public UsuarioBloqueadoException() {
        super(MensagemErro.USUARIO_BLOQUEADO.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
