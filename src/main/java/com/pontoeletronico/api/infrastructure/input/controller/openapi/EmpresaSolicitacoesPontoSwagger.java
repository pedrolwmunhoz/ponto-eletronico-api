package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.registro.*;
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

public interface EmpresaSolicitacoesPontoSwagger {

    @Operation(summary = "Listar ponto do funcionário", description = "Listar informações de ponto de um funcionário (ano/mês). usuarioId da empresa extraído do token JWT. Empresa só tem acesso aos funcionários dela mesma.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<PontoListagemResponse>> listarPonto(@PathVariable("funcionarioId") UUID funcionarioId, @RequestParam int ano, @RequestParam int mes, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Deletar registro de ponto", description = "Deletar registro de ponto de um funcionário. usuarioId da empresa extraído do token JWT. Empresa só tem acesso aos funcionários dela mesma. Soft delete (ativo=false).", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarRegistro(@PathVariable("funcionarioId") UUID funcionarioId, @PathVariable("registroId") UUID registroId, @RequestHeader("Authorization") String authorization);

    @Operation(summary = "Editar registro de ponto", description = "Desativa o registro antigo, cria um novo com os dados enviados e dispara recálculo de banco de horas.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> editarRegistro(@PathVariable("funcionarioId") UUID funcionarioId, @PathVariable("registroId") UUID registroId, @Valid @RequestBody EmpresaCriarRegistroPontoRequest request, @RequestHeader("Authorization") String authorization, @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey, HttpServletRequest httpRequest);

    @Operation(summary = "Criar registro de ponto", description = "Criar registro de ponto para um funcionário. usuarioId da empresa extraído do token JWT. Empresa só tem acesso aos funcionários dela mesma.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> criarRegistro(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody EmpresaCriarRegistroPontoRequest request, @RequestHeader("Authorization") String authorization, @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey, HttpServletRequest httpRequest);

    @Operation(summary = "Listar solicitações de ponto", description = "Listar todas as solicitações de ponto dos funcionários (criar/excluir registro manual). usuarioId da empresa extraído do token JWT.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<SolicitacoesPontoListagemResponse> listarSolicitacoes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestHeader("Authorization") String authorization);

    @Operation(summary = "Aprovar solicitação", description = "Aprovar solicitação de ponto pendente. usuarioId da empresa extraído do token JWT.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> aprovarSolicitacao(@PathVariable("idRegistroPendente") UUID idRegistroPendente, @RequestHeader("Authorization") String authorization, @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey, HttpServletRequest httpRequest);

    @Operation(summary = "Reprovar solicitação", description = "Reprovar solicitação de ponto pendente. usuarioId da empresa extraído do token JWT.", tags = {"Empresa (solicitações de ponto)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> reprovarSolicitacao(@PathVariable("idRegistroPendente") UUID idRegistroPendente, @Valid @RequestBody ReprovarSolicitacaoRequest request, @RequestHeader("Authorization") String authorization);
}
