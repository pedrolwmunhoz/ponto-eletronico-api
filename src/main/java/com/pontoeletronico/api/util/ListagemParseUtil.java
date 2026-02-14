package com.pontoeletronico.api.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemResponse;

import java.util.List;

public final class ListagemParseUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ListagemParseUtil() {}

    public static List<String> parseEmails(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    public static List<UsuarioListagemResponse.TelefoneListagemDto> parseTelefones(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
