package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.CriarAfastamentoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.FeriasAfastamentosListagemResponse;
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

public interface FeriasAfastamentosSwagger {

    @Operation(summary = "Listar férias e afastamentos do funcionário", description = "Listar férias e afastamentos do funcionário. usuarioId (funcionário) extraído do token JWT no backend.", tags = {"Férias e afastamentos"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriasAfastamentosListagemResponse> listarPorFuncionario(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Listar férias e afastamentos de um funcionário por id", description = "Listar férias e afastamentos de um funcionário por id. usuarioId da empresa extraído do token JWT no backend. Empresa só tem acesso aos funcionários dela mesma.", tags = {"Férias e afastamentos"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriasAfastamentosListagemResponse> listarPorFuncionarioIdEmpresa(@PathVariable("funcionarioId") UUID funcionarioId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Listar férias e afastamentos da empresa", description = "Listar férias e afastamentos da empresa. Filtro opcional por nome do funcionário. usuarioId da empresa extraído do token JWT no backend.", tags = {"Férias e afastamentos"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FeriasAfastamentosListagemResponse> listarPorEmpresa(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String nome, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Criar afastamento para um funcionário", description = "Criar afastamento para um funcionário. usuarioId da empresa extraído do token JWT no backend. Empresa só tem acesso aos funcionários dela mesma.", tags = {"Férias e afastamentos"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criarAfastamento(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody CriarAfastamentoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
