package com.pontoeletronico.api.infrastructure.input.dto.geofence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Doc id 46: Criar novo geofence para a empresa - Request. funcionarioIds opcional: IDs (usuarioId) dos funcionários com acesso (acesso parcial). */
public record CriarGeofenceRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,
        @NotBlank(message = "descricao é obrigatória")
        String descricao,
        @NotNull(message = "latitude é obrigatória")
        BigDecimal latitude,
        @NotNull(message = "longitude é obrigatória")
        BigDecimal longitude,
        @NotNull(message = "raioMetros é obrigatório")
        Integer raioMetros,
        Boolean ativo,
        List<UUID> funcionarioIds
) {}
