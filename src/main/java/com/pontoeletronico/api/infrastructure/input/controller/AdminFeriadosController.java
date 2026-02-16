package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.admin.AdminFeriadoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.AdminFeriadosSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.CriarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.EditarFeriadoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.FeriadoListagemPageResponse;
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
@RequestMapping("/api/admin")
public class AdminFeriadosController implements AdminFeriadosSwagger {

    private final AdminFeriadoService adminFeriadoService;

    public AdminFeriadosController(AdminFeriadoService adminFeriadoService) {
        this.adminFeriadoService = adminFeriadoService;
    }

    @GetMapping("/feriados")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<FeriadoListagemPageResponse> listarAbrangencia(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var response = adminFeriadoService.listarAbrangencia(authorization, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feriados")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> criarAbrangencia(@Valid @RequestBody CriarFeriadoRequest request,
                                                @RequestHeader("Authorization") String authorization,
                                                HttpServletRequest httpRequest) {
        adminFeriadoService.criarAbrangencia(authorization, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> editarAbrangencia(@PathVariable UUID feriadoId,
                                                 @Valid @RequestBody EditarFeriadoRequest request,
                                                 @RequestHeader("Authorization") String authorization,
                                                 HttpServletRequest httpRequest) {
        adminFeriadoService.atualizarAbrangencia(authorization, feriadoId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> excluirAbrangencia(@PathVariable UUID feriadoId,
                                                  @RequestHeader("Authorization") String authorization,
                                                  HttpServletRequest httpRequest) {
        adminFeriadoService.deletarAbrangencia(authorization, feriadoId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empresas/{empresaId}/feriados")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<FeriadoListagemPageResponse> listarPorEmpresa(
            @PathVariable UUID empresaId,
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var response = adminFeriadoService.listarPorEmpresa(authorization, empresaId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/empresas/{empresaId}/feriados")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> criarPorEmpresa(@PathVariable UUID empresaId,
                                               @Valid @RequestBody CriarFeriadoRequest request,
                                               @RequestHeader("Authorization") String authorization,
                                               HttpServletRequest httpRequest) {
        adminFeriadoService.criarPorEmpresa(authorization, empresaId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/empresas/{empresaId}/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> editarPorEmpresa(@PathVariable UUID empresaId,
                                                @PathVariable UUID feriadoId,
                                                @Valid @RequestBody EditarFeriadoRequest request,
                                                @RequestHeader("Authorization") String authorization,
                                                HttpServletRequest httpRequest) {
        adminFeriadoService.atualizarPorEmpresa(authorization, empresaId, feriadoId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/empresas/{empresaId}/feriados/{feriadoId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> excluirPorEmpresa(@PathVariable UUID empresaId,
                                                 @PathVariable UUID feriadoId,
                                                 @RequestHeader("Authorization") String authorization,
                                                 HttpServletRequest httpRequest) {
        adminFeriadoService.deletarPorEmpresa(authorization, empresaId, feriadoId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
