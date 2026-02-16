package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemResponse;
import java.util.List;
import java.util.UUID;

/** Doc id 18: Item da listagem de funcionários. Exibe primeiroNome + ultimoNome na tabela. */
public record FuncionarioListagemResponse(
        UUID usuarioId,
        String primeiroNome,
        String ultimoNome,
        String username,
        List<String> emails,
        List<UsuarioListagemResponse.TelefoneListagemDto> telefones
) {}
