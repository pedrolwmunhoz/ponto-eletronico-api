package com.pontoeletronico.api.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Converte entre VARCHAR (ISO-8601, ex: PT8H, PT8H30M) no banco e java.time.Duration na entidade.
 * Aceita também número inteiro (minutos) para compatibilidade com dados legados.
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
            long minutes = Long.parseLong(s);
            return Duration.ofMinutes(minutes);
        } catch (Exception e) {
            log.warn("Valor inválido para Duration no banco: '{}'. Usando PT0S. Esperado: ISO-8601 (ex: PT8H) ou minutos (ex: 480). Erro: {}", dbData, e.getMessage());
            return Duration.ZERO;
        }
    }
}
