package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class CodigoRecuperacaoInvalidoException extends ApiException {

    public CodigoRecuperacaoInvalidoException() {
        super(MensagemErro.CODIGO_RECUPERACAO_INVALIDO.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
