package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.feriasafastamentos.FeriasAfastamentosService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.FeriasAfastamentosSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.CriarAfastamentoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.FeriasAfastamentosListagemResponse;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api")
public class FeriasAfastamentosController implements FeriasAfastamentosSwagger {

    private final FeriasAfastamentosService feriasAfastamentosService;
    private final JwtUtil jwtUtil;

    public FeriasAfastamentosController(FeriasAfastamentosService feriasAfastamentosService, JwtUtil jwtUtil) {
        this.feriasAfastamentosService = feriasAfastamentosService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/funcionario/ferias-afastamentos")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<FeriasAfastamentosListagemResponse> listarPorFuncionario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        var response = feriasAfastamentosService.listarPorFuncionario(funcionarioId, page, size, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/empresa/funcionario/{funcionarioId}/ferias-afastamentos")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<FeriasAfastamentosListagemResponse> listarPorFuncionarioIdEmpresa(
            @PathVariable UUID funcionarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = feriasAfastamentosService.listarPorFuncionarioIdEmpresa(empresaId, funcionarioId, page, size, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/empresa/ferias-afastamentos")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<FeriasAfastamentosListagemResponse> listarPorEmpresa(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nome,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = feriasAfastamentosService.listarPorEmpresa(empresaId, page, size, nome, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/empresa/funcionario/{funcionarioId}/afastamentos")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> criarAfastamento(
            @PathVariable UUID funcionarioId,
            @Valid @RequestBody CriarAfastamentoRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        feriasAfastamentosService.criarAfastamento(empresaId, funcionarioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
