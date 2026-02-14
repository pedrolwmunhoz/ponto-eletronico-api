package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.perfil.EmpresaPerfilService;
import com.pontoeletronico.api.domain.services.perfil.FuncionarioPerfilService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.PerfilSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.EmpresaPerfilResponse;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.FuncionarioPerfilResponse;
import com.pontoeletronico.api.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
public class PerfilController implements PerfilSwagger {

    private final FuncionarioPerfilService funcionarioPerfilService;
    private final EmpresaPerfilService empresaPerfilService;
    private final JwtUtil jwtUtil;

    public PerfilController(FuncionarioPerfilService funcionarioPerfilService,
                            EmpresaPerfilService empresaPerfilService,
                            JwtUtil jwtUtil) {
        this.funcionarioPerfilService = funcionarioPerfilService;
        this.empresaPerfilService = empresaPerfilService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/funcionario/perfil")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<FuncionarioPerfilResponse> buscarPerfilFuncionario(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(funcionarioPerfilService.buscar(funcionarioId, httpRequest));
    }

    @GetMapping("/empresa/perfil")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<EmpresaPerfilResponse> buscarPerfilEmpresa(@RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = empresaPerfilService.buscar(empresaId);
        return ResponseEntity.ok(response);
    }
}
