package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

/** Registrar compensação banco de horas - Request. */
public record BancoHorasCompensacaoRequest(
        @NotNull(message = "historicoId é obrigatório")
        UUID historicoId,
        @NotNull(message = "minutos é obrigatório")
        Integer minutos,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataCompensacao,
        String observacao
) {}
