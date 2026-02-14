package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.registro.*;
import com.pontoeletronico.api.infrastructure.input.dto.registro.PontoListagemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.registro.RegistroPontoManualRequest;
import com.pontoeletronico.api.infrastructure.input.dto.registro.RegistroPontoPublicoRequest;
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

import java.util.List;
import java.util.UUID;

public interface FuncionarioRegistroPontoSwagger {

    @Operation(summary = "Listar ponto do funcionário", description = "Listar informações de ponto do funcionário (ano/mês). usuarioId (funcionário) extraído do token JWT.", tags = {"Registro-ponto (funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<PontoListagemResponse>> listarPontoFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @RequestParam int ano, @RequestParam int mes, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Criar registro de ponto manual", description = "Criar novo registro de ponto manualmente (funcionário). usuarioId (funcionário) extraído do token JWT.", tags = {"Registro-ponto (funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> registrarManual(@Valid @RequestBody RegistroPontoManualRequest request, @RequestHeader("Authorization") String authorization, @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey, HttpServletRequest httpRequest);

    @Operation(summary = "Registro de ponto público", description = "Registro de ponto público (tablet da empresa). usuarioId da empresa extraído do token JWT. Header Idempotency-Key obrigatório.", tags = {"Registro-ponto (funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "409", description = "Conflict (idempotência)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> registrarPublico(@Valid @RequestBody RegistroPontoPublicoRequest request, @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Registro de ponto pelo aplicativo", description = "Registro de ponto pelo aplicativo. usuarioId (funcionário) extraído do token JWT. Header Idempotency-Key obrigatório.", tags = {"Registro-ponto (funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "409", description = "Conflict (idempotência)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> registrarPontoFuncionario(@Valid @RequestBody RegistroPontoAppRequest request, @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Deletar registro de ponto", description = "Deletar registro de ponto (funcionário). usuarioId (funcionário) extraído do token JWT. Soft delete (ativo=false).", tags = {"Registro-ponto (funcionário)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarRegistro(@PathVariable("idRegistro") UUID idRegistro, @RequestHeader("Authorization") String authorization, @RequestBody RegistroMetadadosRequest request);
}
