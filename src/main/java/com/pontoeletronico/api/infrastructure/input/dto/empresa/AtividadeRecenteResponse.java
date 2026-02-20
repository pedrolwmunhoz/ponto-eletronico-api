package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import java.time.LocalDateTime;

/** Um item da lista de atividades recentes (últimos registros de ponto da empresa). */
public record AtividadeRecenteResponse(
        String nomeCompleto,
        LocalDateTime registradoEm
) {}
