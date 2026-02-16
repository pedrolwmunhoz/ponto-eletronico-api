package com.pontoeletronico.api.infrastructure.input.dto.geofence;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Doc id 46: Criar novo geofence para a empresa - Request. funcionarioIds opcional: IDs (usuarioId) dos funcionários com acesso (acesso parcial). */
public record CriarGeofenceRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 2, max = 255, message = "nome deve ter entre 2 e 255 caracteres")
        String nome,
        @NotBlank(message = "descricao é obrigatória")
        @Size(min = 2, max = 500, message = "descricao deve ter entre 2 e 500 caracteres")
        String descricao,
        @NotNull(message = "latitude é obrigatória")
        @Min(value = -90, message = "latitude deve estar entre -90 e 90")
        @Max(value = 90, message = "latitude deve estar entre -90 e 90")
        BigDecimal latitude,
        @NotNull(message = "longitude é obrigatória")
        @Min(value = -180, message = "longitude deve estar entre -180 e 180")
        @Max(value = 180, message = "longitude deve estar entre -180 e 180")
        BigDecimal longitude,
        @NotNull(message = "raioMetros é obrigatório")
        @Min(value = 1, message = "raioMetros deve estar entre 1 e 50000")
        @Max(value = 50000, message = "raioMetros deve estar entre 1 e 50000")
        Integer raioMetros,
        Boolean ativo,
        List<UUID> funcionarioIds
) {}
