package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Doc id 44b: Listar histórico banco de horas - Response paginada. */
public record BancoHorasHistoricoPageResponse(
        Paginacao paginacao,
        List<BancoHorasHistoricoResponse> conteudo
) {}
