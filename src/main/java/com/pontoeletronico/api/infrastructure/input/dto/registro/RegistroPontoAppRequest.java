package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.Valid;

/** Doc id 31: Batida (app). Data/hora definida pelo backend (instante da requisição); front não envia. */
public record RegistroPontoAppRequest(
        @Valid
        RegistroMetadadosRequest registroMetadados
) {}
