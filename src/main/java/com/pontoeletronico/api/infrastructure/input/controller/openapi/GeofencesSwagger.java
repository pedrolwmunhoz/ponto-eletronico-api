package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.geofence.CriarGeofenceRequest;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.GeofenceListagemPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface GeofencesSwagger {

    @Operation(summary = "Listar geofences da empresa", description = "Listar geofences da empresa com paginação. Params: page, size.", tags = {"Geofences"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<GeofenceListagemPageResponse> listar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest);

    @Operation(summary = "Criar novo geofence para a empresa", description = "Criar novo geofence para a empresa. usuarioId da empresa extraído do token JWT no backend.", tags = {"Geofences"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criar(@Valid @RequestBody CriarGeofenceRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
