package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class NaoAdminException extends ApiException {

    public NaoAdminException() {
        super(MensagemErro.NAO_ADMIN.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
