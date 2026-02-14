package com.pontoeletronico.api.infrastructure.input.dto.admin;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Doc id 52: Listar usuários (admin) - Response. */
public record UsuarioListagemPageResponse(
        Paginacao paginacao,
        List<UsuarioListagemResponse> conteudo
) {}
