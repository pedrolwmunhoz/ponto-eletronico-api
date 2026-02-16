package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioCredentialAdicionarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioEmailRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioPerfilAtualizarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioTelefoneAdicionarRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

public interface UsuarioSwagger {

    @Operation(summary = "Atualizar username", description = "Atualiza o username do usuário logado. usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "409", description = "Username já em uso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarPerfil(@Valid @RequestBody UsuarioPerfilAtualizarRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Atualizar email", description = "Atualiza o email primário (remove antigo e insere novo em transação). usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "404", description = "Email primário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Novo email já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarEmail(@Valid @RequestBody UsuarioEmailRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Adicionar novo email", description = "Adiciona novo email como credencial. usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> adicionarEmail(@Valid @RequestBody UsuarioEmailRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Remover email", description = "Remove email (soft delete). usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email removido"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "404", description = "Email não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> removerEmail(@Valid @RequestBody UsuarioEmailRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Adicionar novo telefone", description = "Adiciona novo telefone. usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Telefone adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "409", description = "Telefone já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> adicionarTelefone(@Valid @RequestBody UsuarioTelefoneAdicionarRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Deletar telefone", description = "Remove telefone (delete físico). usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Telefone removido"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "404", description = "Telefone não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> removerTelefone(@PathVariable("telefoneId") UUID telefoneId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Adicionar novo tipo de login", description = "Adiciona nova credencial (ex: outro email, telefone). usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Credencial adicionada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "409", description = "Valor já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> adicionarCredential(@Valid @RequestBody UsuarioCredentialAdicionarRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Deletar tipo de login", description = "Remove credencial (soft delete). usuarioId extraído do token JWT.", tags = {"Usuario"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Credencial removida"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "404", description = "Credencial não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> removerCredential(@PathVariable("credentialId") UUID credentialId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
