package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class FuncionarioNaoEncontradoException extends ApiException {

    public FuncionarioNaoEncontradoException() {
        super(MensagemErro.FUNCIONARIO_NAO_ENCONTRADO.getMensagem(), HttpStatus.UNAUTHORIZED);
    }
}
