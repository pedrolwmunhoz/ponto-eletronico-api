package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request para fechamento do mês - grava em banco_horas_historico. */
public record FechamentoBancoHorasRequest(
        @NotNull @Min(2000) @Max(2100)
        Integer anoReferencia,
        @NotNull @Min(1) @Max(12)
        Integer mesReferencia
) {}
