package com.pontoeletronico.api.infrastructure.input.dto.admin;

import java.util.List;
import java.util.UUID;

/** Doc id 52: Item da listagem de usuários (admin). */
public record UsuarioListagemResponse(
        UUID usuarioId,
        String username,
        String tipo,
        List<String> emails,
        List<TelefoneListagemDto> telefones
) {
    public record TelefoneListagemDto(String codigoPais, String ddd, String numero) {}
}
