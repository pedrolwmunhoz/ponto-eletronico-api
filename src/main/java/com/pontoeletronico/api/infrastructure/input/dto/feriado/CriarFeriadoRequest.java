package com.pontoeletronico.api.infrastructure.input.dto.feriado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Doc id 53: Criar feriado para a empresa - Request. usuarioId é preenchido no service a partir do JWT. */
public record CriarFeriadoRequest(
        @NotNull(message = "data é obrigatória")
        LocalDate data,
        @NotBlank(message = "descricao é obrigatória")
        @Size(min = 2, max = 255, message = "descricao deve ter entre 2 e 255 caracteres")
        String descricao,
        @NotNull(message = "tipoFeriadoId é obrigatório")
        Integer tipoFeriadoId,
        Boolean ativo
) {}
