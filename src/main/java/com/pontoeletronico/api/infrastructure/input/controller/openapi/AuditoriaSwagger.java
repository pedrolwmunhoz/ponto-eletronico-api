package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaDetalheResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaListagemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface AuditoriaSwagger {

    @Operation(summary = "Listar log de auditoria", description = "Listar log de auditoria. usuarioId da empresa extraído do token JWT no backend.", tags = {"Auditoria"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<AuditoriaListagemResponse> listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Detalhar log de auditoria", description = "Detalhar log de auditoria. usuarioId da empresa extraído do token JWT no backend.", tags = {"Auditoria"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<AuditoriaDetalheResponse> detalhar(@PathVariable("logId") UUID logId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
