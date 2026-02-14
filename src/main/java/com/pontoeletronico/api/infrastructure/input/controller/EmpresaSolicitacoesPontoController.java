package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.empresa.EmpresaSolicitacoesPontoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.EmpresaSolicitacoesPontoSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.registro.*;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa")
public class EmpresaSolicitacoesPontoController implements EmpresaSolicitacoesPontoSwagger {

    private final EmpresaSolicitacoesPontoService empresaSolicitacoesPontoService;
    private final JwtUtil jwtUtil;

    public EmpresaSolicitacoesPontoController(EmpresaSolicitacoesPontoService empresaSolicitacoesPontoService, JwtUtil jwtUtil) {
        this.empresaSolicitacoesPontoService = empresaSolicitacoesPontoService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/funcionario/{funcionarioId}/ponto")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<List<PontoListagemResponse>> listarPonto(@PathVariable UUID funcionarioId,
                                                             @RequestParam int ano,
                                                             @RequestParam int mes,
                                                             @RequestHeader("Authorization") String authorization,
                                                             HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = empresaSolicitacoesPontoService.listarPonto(empresaId, funcionarioId, ano, mes, httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/funcionario/{funcionarioId}/registro-ponto/{registroId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> deletarRegistro(@PathVariable UUID funcionarioId,
                                                @PathVariable UUID registroId,
                                                @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaSolicitacoesPontoService.empresaDeletarRegistroManual(empresaId, funcionarioId, registroId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/funcionario/{funcionarioId}/registro-ponto/{registroId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> editarRegistro(@PathVariable UUID funcionarioId,
                                              @PathVariable UUID registroId,
                                              @Valid @RequestBody EmpresaCriarRegistroPontoRequest request,
                                              @RequestHeader("Authorization") String authorization,
                                              @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey,
                                              HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaSolicitacoesPontoService.empresaEditarRegistroManual(empresaId, idempotencyKey, funcionarioId, registroId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/funcionario/{funcionarioId}/registro-ponto")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> criarRegistro(@PathVariable UUID funcionarioId,
                                              @Valid @RequestBody EmpresaCriarRegistroPontoRequest request,
                                              @RequestHeader("Authorization") String authorization,
                                              @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey,
                                              HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaSolicitacoesPontoService.empresaCriarRegistroManual(empresaId, idempotencyKey, funcionarioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/solicitacoes-ponto")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<SolicitacoesPontoListagemResponse> listarSolicitacoes(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size,
                                                                                @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = empresaSolicitacoesPontoService.listarSolicitacoes(empresaId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solicitacoes-ponto/{idRegistroPendente}/aprovar")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> aprovarSolicitacao(@PathVariable UUID idRegistroPendente,
                                                   @RequestHeader("Authorization") String authorization,
                                                   @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey,
                                                   HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaSolicitacoesPontoService.aprovar(empresaId, idempotencyKey, idRegistroPendente, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/solicitacoes-ponto/{idRegistroPendente}/reprovar")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> reprovarSolicitacao(@PathVariable UUID idRegistroPendente,
                                                   @Valid @RequestBody ReprovarSolicitacaoRequest request,
                                                   @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaSolicitacoesPontoService.reprovar(empresaId, idRegistroPendente, request);
        return ResponseEntity.ok().build();
    }
}
