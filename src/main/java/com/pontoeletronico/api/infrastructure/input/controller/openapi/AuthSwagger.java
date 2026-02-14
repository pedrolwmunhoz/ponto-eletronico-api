package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface AuthSwagger {

    @Operation(summary = "Login", description = "Autentica o usuário e retorna JWT e refresh token", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credencial inválida ou erro de validação"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Usuário inativo ou bloqueado"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login. Tente novamente mais tarde."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Recuperar senha", description = "Gera código de recuperação e envia por email", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código enviado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Email não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Validar código", description = "Valida código de recuperação e retorna token temporário", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token retornado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Código inválido ou expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ValidarCodigoResponse> validarCodigo(@Valid @RequestBody ValidarCodigoRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Resetar senha", description = "Reseta senha com token temporário", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> resetarSenha(@Valid @RequestBody ResetarSenhaRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Refresh token", description = "Renova JWT e refresh token", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novos tokens retornados"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Logout", description = "Invalida sessão do usuário", tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
