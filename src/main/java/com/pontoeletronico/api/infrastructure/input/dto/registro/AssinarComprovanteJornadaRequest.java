package com.pontoeletronico.api.infrastructure.input.dto.registro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload a ser assinado para o comprovante PDF da jornada (ex.: hash Base64 do PDF ou do conteúdo).
 * Backend assina com o certificado A1 da empresa e retorna assinatura; não persiste nada.
 */
@Schema(description = "Payload do comprovante a assinar (ex.: Base64 do hash SHA-256 do PDF)")
public record AssinarComprovanteJornadaRequest(
        @NotBlank(message = "payloadBase64 é obrigatório")
        @Schema(description = "Payload em Base64 a ser assinado (ex.: hash do PDF)", requiredMode = Schema.RequiredMode.REQUIRED)
        String payloadBase64
) {}
