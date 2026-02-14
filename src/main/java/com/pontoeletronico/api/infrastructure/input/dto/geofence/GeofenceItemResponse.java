package com.pontoeletronico.api.infrastructure.input.dto.geofence;

import java.time.LocalDateTime;
import java.util.UUID;

/** Doc id 45: Listar geofences da empresa - Item da listagem. id para uso em geofenceIds (cadastro/atualização de funcionário). acessoParcial: true quando há funcionários específicos (xref); quantidadeFuncionariosAcesso: 0 = todos, >0 = parcial com N. */
public record GeofenceItemResponse(
        UUID id,
        String nome,
        Boolean ativo,
        String coordenadas,
        Integer raio,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean acessoParcial,
        Integer quantidadeFuncionariosAcesso
) {}
