package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Doc id 18: Listar funcionários da empresa - Response. */
public record FuncionarioListagemPageResponse(
        Paginacao paginacao,
        List<FuncionarioListagemResponse> conteudo
) {}
