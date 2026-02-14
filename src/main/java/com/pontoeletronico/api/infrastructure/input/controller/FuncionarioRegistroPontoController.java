package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.registro.FuncionarioRegistroPontoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.FuncionarioRegistroPontoSwagger;
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
@RequestMapping("/api")
public class FuncionarioRegistroPontoController implements FuncionarioRegistroPontoSwagger {

    private final FuncionarioRegistroPontoService registroPontoService;
    private final JwtUtil jwtUtil;

    public FuncionarioRegistroPontoController(FuncionarioRegistroPontoService registroPontoService, JwtUtil jwtUtil) {
        this.registroPontoService = registroPontoService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/funcionario/{funcionarioId}/ponto")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<List<PontoListagemResponse>> listarPontoFuncionario(@PathVariable UUID funcionarioId,
                                                                       @RequestParam int ano,
                                                                       @RequestParam int mes,
                                                                       @RequestHeader("Authorization") String authorization,
                                                                       HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        if (!funcionarioId.equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var response = registroPontoService.listarPontoFuncionario(funcionarioId, ano, mes, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/funcionario/registro-ponto/manual")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> registrarManual(@Valid @RequestBody RegistroPontoManualRequest request,
                                               @RequestHeader("Authorization") String authorization,
                                               @RequestHeader(value = "Idempotency-Key", required = true) UUID idempotencyKey,
                                               HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        registroPontoService.registrarPontoManualFuncionario(funcionarioId, idempotencyKey, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/empresa/registro-ponto/publico")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> registrarPublico(@Valid @RequestBody RegistroPontoPublicoRequest request,
                                                @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
                                                @RequestHeader("Authorization") String authorization,
                                                HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        registroPontoService.registrarPontoAppPublicoFuncionario(empresaId, request, idempotencyKey, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/empresa/funcionario/registro-ponto")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> registrarPontoFuncionario(@Valid @RequestBody RegistroPontoAppRequest request,
                                            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
                                            @RequestHeader("Authorization") String authorization,
                                            HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        registroPontoService.registrarPontoAppIndividualFuncionario(funcionarioId, idempotencyKey, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/empresa/registro-ponto/{idRegistro}")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> deletarRegistro(@PathVariable UUID idRegistro,
                                               @RequestHeader("Authorization") String authorization,
                                               @RequestBody RegistroMetadadosRequest request) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        registroPontoService.deletarRegistroFuncionario(funcionarioId, idRegistro, request);
        return ResponseEntity.noContent().build();
    }
}
