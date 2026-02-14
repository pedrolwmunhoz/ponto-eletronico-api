package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class UsuarioInativoException extends ApiException {

    public UsuarioInativoException() {
        super(MensagemErro.USUARIO_INATIVO.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
