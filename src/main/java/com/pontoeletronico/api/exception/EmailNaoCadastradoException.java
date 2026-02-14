package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class EmailNaoCadastradoException extends ApiException {

    public EmailNaoCadastradoException() {
        super(MensagemErro.EMAIL_NAO_CADASTRADO.getMensagem(), HttpStatus.NOT_FOUND);
    }
}
