package com.pontoeletronico.api.infrastructure.input.dto.registro;

import java.time.LocalDateTime;

/** Doc id 30/31: Metadados do registro (geo, assinatura) - usado em registro público e app. */
public record RegistroMetadadosRequest(
        Double geoLatitude,
        Double geoLongitude,
        String assinaturaDigital,
        String certificadoSerial,
        LocalDateTime timestampAssinatura
) {}
