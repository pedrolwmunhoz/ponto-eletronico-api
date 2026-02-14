package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class DispositivoNaoIdentificadoException extends ApiException {

    public DispositivoNaoIdentificadoException() {
        super(MensagemErro.IP_USER_AGENT_OBRIGATORIOS.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
