package com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Doc id 42: Criar afastamento para um funcionário - Request. */
public record CriarAfastamentoRequest(
        @NotNull(message = "tipoAfastamentoId é obrigatório")
        Integer tipoAfastamentoId,
        @NotNull(message = "dataInicio é obrigatória")
        LocalDate dataInicio,
        LocalDate dataFim,
        String observacao,
        Boolean ativo
) {}
