package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Doc id 35: Registro manual — front envia horário sem offset (ex: 2026-02-01T09:12:00). */
public record EmpresaCriarRegistroPontoRequest(
        @NotNull(message = "horario é obrigatório")
        LocalDateTime horario,
        @NotNull(message = "justificativa é obrigatório")
        @NotBlank(message = "justificativa não pode ser vazia")
        @Size(min = 1, max = 500, message = "justificativa deve ter entre 1 e 500 caracteres")
        String justificativa,
        String observacao
) {}
