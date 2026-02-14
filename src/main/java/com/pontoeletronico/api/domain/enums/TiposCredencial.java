package com.pontoeletronico.api.domain.enums;

public enum TiposCredencial {

    EMAIL,
    TELEFONE,
    CPF,
    CNPJ,
    USERNAME;

    public String getName() {
        return this.name();
    }
}
