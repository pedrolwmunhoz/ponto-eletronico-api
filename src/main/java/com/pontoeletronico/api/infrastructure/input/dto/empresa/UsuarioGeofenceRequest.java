package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Geofence do funcionário (ex: home office)")
public record UsuarioGeofenceRequest(
        @NotBlank(message = "descricao é obrigatória")
        @Size(max = 255, message = "descricao deve ter no máximo 255 caracteres")
        @Schema(description = "Descrição do local", example = "Home Office", requiredMode = Schema.RequiredMode.REQUIRED)
        String descricao,
        
        @NotNull(message = "latitude é obrigatória")
        @Schema(description = "Latitude", example = "-23.5505", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal latitude,
        
        @NotNull(message = "longitude é obrigatória")
        @Schema(description = "Longitude", example = "-46.6333", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal longitude,
        
        @NotNull(message = "raioMetros é obrigatório")
        @Min(value = 1, message = "raioMetros deve ser entre 1 e 5000")
        @Max(value = 5000, message = "raioMetros deve ser entre 1 e 5000")
        @Schema(description = "Raio em metros", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer raioMetros,
        
        @Schema(description = "Geofence ativo", example = "true")
        boolean ativo
) {}
