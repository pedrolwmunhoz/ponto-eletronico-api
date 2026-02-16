package com.pontoeletronico.api.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Converte entre VARCHAR (ISO-8601, ex: PT8H, PT8H30M) no banco e java.time.Duration na entidade.
 * Aceita também número: ISO-8601 (PT8H), minutos (ex: 480) ou nanossegundos (ex: 39600000000000 = 11h)
 * para compatibilidade com dados legados ou serialização incorreta.
 */
@Converter(autoApply = false)
public class DurationStringConverter implements AttributeConverter<Duration, String> {

    private static final Logger log = LoggerFactory.getLogger(DurationStringConverter.class);

    @Override
    public String convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) return null;
        return attribute.toString();
    }

    @Override
    public Duration convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Duration.ZERO;
        String s = dbData.trim();
        try {
            if (s.startsWith("PT") || s.startsWith("-PT")) {
                return Duration.parse(s);
            }
            long n = Long.parseLong(s);
            if (n >= 1_000_000_000L || n <= -1_000_000_000L) {
                return Duration.ofNanos(n);
            }
            return Duration.ofMinutes(n);
        } catch (Exception e) {
            log.warn("Valor inválido para Duration no banco: '{}'. Usando PT0S. Esperado: ISO-8601 (ex: PT8H), minutos (ex: 480) ou nanos. Erro: {}", dbData, e.getMessage());
            return Duration.ZERO;
        }
    }
}
