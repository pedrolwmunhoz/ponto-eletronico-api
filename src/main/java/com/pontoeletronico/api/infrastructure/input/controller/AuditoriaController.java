package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.auditoria.AuditoriaService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.AuditoriaSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaDetalheResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaListagemResponse;
import com.pontoeletronico.api.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa")
public class AuditoriaController implements AuditoriaSwagger {

    private final AuditoriaService auditoriaService;
    private final JwtUtil jwtUtil;

    public AuditoriaController(AuditoriaService auditoriaService, JwtUtil jwtUtil) {
        this.auditoriaService = auditoriaService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/auditoria")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<AuditoriaListagemResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = auditoriaService.listarPorEmpresa(empresaId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auditoria/{logId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<AuditoriaDetalheResponse> detalhar(
            @PathVariable UUID logId,
            @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = auditoriaService.detalhar(empresaId, logId);
        return ResponseEntity.ok(response);
    }
}
