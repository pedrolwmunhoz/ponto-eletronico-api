package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.perfil.EmpresaPerfilResponse;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.FuncionarioPerfilResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

public interface PerfilSwagger {

    @Operation(summary = "Recuperar informações do funcionário", description = "Recuperar informações do funcionário. usuarioId extraído do token JWT no backend.", tags = {"Perfil (empresa, funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações do funcionário"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FuncionarioPerfilResponse> buscarPerfilFuncionario(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Recuperar informações da empresa", description = "Recuperar informações da empresa. usuarioId extraído do token JWT no backend.", tags = {"Perfil (empresa, funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações da empresa"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<EmpresaPerfilResponse> buscarPerfilEmpresa(@RequestHeader("Authorization") String authorization);
}
