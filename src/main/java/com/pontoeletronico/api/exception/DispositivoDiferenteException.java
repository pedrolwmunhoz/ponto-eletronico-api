package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class DispositivoDiferenteException extends ApiException {

    public DispositivoDiferenteException() {
        super(MensagemErro.DISPOSITIVO_DIFERENTE.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
