package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.feriado.CriarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.EditarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.FeriadoListagemPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

public interface FeriadosSwagger {

    @Operation(summary = "Listar feriados da empresa", description = "Listar feriados da empresa com paginação. Params: page, size, observacao (opcional), dataInicio (opcional, ISO date), dataFim (opcional, ISO date).", tags = {"Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriadoListagemPageResponse> listar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String observacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletRequest httpRequest);

    @Operation(summary = "Criar novo feriado para a empresa", description = "Criar novo feriado para a empresa. empresaId extraído do token JWT no backend.", tags = {"Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criar(@Valid @RequestBody CriarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Editar feriado", description = "Editar feriado existente. Feriado deve pertencer à empresa do token.", tags = {"Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Feriado não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> editar(@PathVariable("feriadoId") UUID feriadoId, @Valid @RequestBody EditarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Excluir feriado", description = "Excluir feriado (soft delete). Feriado deve pertencer à empresa do token.", tags = {"Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Feriado não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> excluir(@PathVariable("feriadoId") UUID feriadoId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
