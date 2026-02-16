package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.admin.AdminCriarRequest;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface AdminSwagger {

    @Operation(summary = "Criar administrador", description = "Cria um novo usuário administrador. Público — não requer autenticação.", tags = {"Admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Administrador criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Username ou valor da credencial já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<UUID> criarAdmin(@Valid @RequestBody AdminCriarRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Listar usuários", description = "Lista todos os usuários (usuarioId, username, tipo, emails, telefones) com paginação. Requer JWT de administrador.", tags = {"Admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários paginada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<UsuarioListagemPageResponse> listarUsuarios(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest);

    @Operation(summary = "Desbloquear usuário", description = "Desativa bloqueio de qualquer usuário. Requer JWT de administrador.", tags = {"Admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores."),
            @ApiResponse(responseCode = "404", description = "Bloqueio não encontrado para o usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> desbloquearUsuario(@PathVariable("usuarioId") UUID usuarioId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
