package com.pontoeletronico.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Corpo padronizado de retorno para respostas de erro da API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        @JsonProperty("mensagem") String mensagem,
        @JsonProperty("status") int status,
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("path") String path
) {
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static ErroResponse of(String mensagem, int status) {
        return new ErroResponse(mensagem, status, ISO_FORMAT.format(LocalDateTime.now()), null);
    }

    public static ErroResponse of(String mensagem, int status, String path) {
        return new ErroResponse(mensagem, status, ISO_FORMAT.format(LocalDateTime.now()), path);
    }
}
