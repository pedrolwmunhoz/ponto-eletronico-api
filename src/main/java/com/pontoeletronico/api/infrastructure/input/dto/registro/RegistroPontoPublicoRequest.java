package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/** Doc id 30: Batida (tablet). Data/hora definida pelo backend (instante da requisição); front não envia. */
public record RegistroPontoPublicoRequest(
        @NotNull(message = "codigoPonto é obrigatório")
        @Max(999999)
        Integer codigoPonto,
        @Valid
        RegistroMetadadosRequest registroMetadados
) {}
