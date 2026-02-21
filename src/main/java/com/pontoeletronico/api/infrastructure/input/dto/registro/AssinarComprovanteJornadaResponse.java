package com.pontoeletronico.api.infrastructure.input.dto.registro;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resposta da assinatura do comprovante de jornada. Campos nulos quando a empresa não tem certificado.
 */
@Schema(description = "Resposta da assinatura do comprovante (assinatura digital, serial do certificado, timestamp)")
public record AssinarComprovanteJornadaResponse(
        @Schema(description = "Assinatura digital em Base64 (null se empresa sem certificado)")
        String assinaturaDigital,
        @Schema(description = "Número de série do certificado (null se empresa sem certificado)")
        String certificadoSerial,
        @Schema(description = "Data/hora da assinatura (null se empresa sem certificado)")
        LocalDateTime timestampAssinatura
) {}
