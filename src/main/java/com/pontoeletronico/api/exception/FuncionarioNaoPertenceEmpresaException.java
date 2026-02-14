package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;

public class FuncionarioNaoPertenceEmpresaException extends ApiException {

    public FuncionarioNaoPertenceEmpresaException() {
        super(MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), HttpStatus.FORBIDDEN);
    }
}
