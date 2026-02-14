package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/** Resposta da métrica diária da empresa (registro de hoje ou o último cadastrado). */
public record MetricasDiariaEmpresaResponse(
        UUID id,
        LocalDate dataRef,
        Integer anoRef,
        Integer mesRef,
        Integer quantidadeFuncionarios,
        Integer solicitacoesPendentes,
        Duration totalDoDia,
        Integer totalPontoHoje
) {}
