package com.pontoeletronico.api.infrastructure.input.dto.geofence;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;

import java.util.List;

/** Listar geofences da empresa - Response paginada (igual admin/usuarios). */
public record GeofenceListagemPageResponse(
        Paginacao paginacao,
        List<GeofenceItemResponse> conteudo
) {}
