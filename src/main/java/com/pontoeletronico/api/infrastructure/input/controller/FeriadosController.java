package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.feriado.FeriadoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.FeriadosSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.CriarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.EditarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.FeriadoListagemPageResponse;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa")
public class FeriadosController implements FeriadosSwagger {

    private final FeriadoService feriadoService;
    private final JwtUtil jwtUtil;

    public FeriadosController(FeriadoService feriadoService, JwtUtil jwtUtil) {
        this.feriadoService = feriadoService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/feriados")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<FeriadoListagemPageResponse> listar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String observacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = feriadoService.listarPorEmpresa(empresaId, page, size, observacao, dataInicio, dataFim, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feriados")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> criar(@Valid @RequestBody CriarFeriadoRequest request,
                                     @RequestHeader("Authorization") String authorization,
                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        feriadoService.criar(empresaId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> editar(@PathVariable UUID feriadoId,
                                      @Valid @RequestBody EditarFeriadoRequest request,
                                      @RequestHeader("Authorization") String authorization,
                                      HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        feriadoService.atualizar(empresaId, feriadoId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> excluir(@PathVariable UUID feriadoId,
                                       @RequestHeader("Authorization") String authorization,
                                       HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        feriadoService.deletar(empresaId, feriadoId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
