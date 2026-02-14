package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class TentativasExcedidasException extends ApiException {

    public TentativasExcedidasException() {
        super(MensagemErro.TENTATIVAS_EXCEDIDAS.getMensagem(), HttpStatus.TOO_MANY_REQUESTS);
    }
}
