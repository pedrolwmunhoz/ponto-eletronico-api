package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class ErroAtualizacaoSenhaException extends ApiException {

    public ErroAtualizacaoSenhaException() {
        super(MensagemErro.ERRO_ATUALIZACAO_SENHA.getMensagem(), HttpStatus.BAD_REQUEST);
    }
}
