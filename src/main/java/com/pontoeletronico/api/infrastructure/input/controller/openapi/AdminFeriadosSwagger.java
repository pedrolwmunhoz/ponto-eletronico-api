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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface AdminFeriadosSwagger {

    @Operation(summary = "Admin: Listar feriados de abrangência", description = "Lista feriados com empresa_id NULL (criados pelo admin). NACIONAL, ESTADUAL, MUNICIPAL.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriadoListagemPageResponse> listarAbrangencia(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @Operation(summary = "Admin: Criar feriado de abrangência", description = "Cria feriado com empresa_id NULL. Qualquer tipo: NACIONAL, ESTADUAL, MUNICIPAL.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criarAbrangencia(@Valid @RequestBody CriarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Admin: Editar feriado de abrangência", description = "Edita feriado com empresa_id NULL.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Feriado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> editarAbrangencia(@PathVariable("feriadoId") UUID feriadoId, @Valid @RequestBody EditarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Admin: Excluir feriado de abrangência", description = "Exclui (soft delete) feriado com empresa_id NULL.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Feriado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> excluirAbrangencia(@PathVariable("feriadoId") UUID feriadoId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Admin: Listar feriados por empresa", description = "Lista feriados da empresa especificada.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriadoListagemPageResponse> listarPorEmpresa(
            @PathVariable("empresaId") UUID empresaId,
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @Operation(summary = "Admin: Criar feriado por empresa", description = "Cria feriado para a empresa. Qualquer tipo.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criarPorEmpresa(@PathVariable("empresaId") UUID empresaId, @Valid @RequestBody CriarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Admin: Editar feriado por empresa", description = "Edita feriado da empresa.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Feriado ou empresa não encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> editarPorEmpresa(@PathVariable("empresaId") UUID empresaId, @PathVariable("feriadoId") UUID feriadoId, @Valid @RequestBody EditarFeriadoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Admin: Excluir feriado por empresa", description = "Exclui (soft delete) feriado da empresa.", tags = {"Admin - Feriados"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Não é admin"),
            @ApiResponse(responseCode = "404", description = "Feriado ou empresa não encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> excluirPorEmpresa(@PathVariable("empresaId") UUID empresaId, @PathVariable("feriadoId") UUID feriadoId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
