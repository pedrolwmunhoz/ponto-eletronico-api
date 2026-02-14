package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.bancohoras.BancoHorasService;
import com.pontoeletronico.api.domain.services.bancohoras.FechamentoBancoHorasService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.BancoHorasSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasCompensacaoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FechamentoBancoHorasRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ResumoBancoHorasResponse;
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
@RequestMapping("/api/empresa")
public class BancoHorasController implements BancoHorasSwagger {

    private final BancoHorasService bancoHorasService;
    private final FechamentoBancoHorasService fechamentoBancoHorasService;
    private final JwtUtil jwtUtil;

    public BancoHorasController(BancoHorasService bancoHorasService, FechamentoBancoHorasService fechamentoBancoHorasService, JwtUtil jwtUtil) {
        this.bancoHorasService = bancoHorasService;
        this.fechamentoBancoHorasService = fechamentoBancoHorasService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/funcionario/{funcionarioId}/resumo-banco-horas")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<ResumoBancoHorasResponse> resumoBancoHoras(@PathVariable UUID funcionarioId,
                                                                      @RequestHeader("Authorization") String authorization,
                                                                      HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = bancoHorasService.resumoBancoHoras(empresaId, funcionarioId, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/funcionario/{funcionarioId}/banco-horas-historico")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<BancoHorasHistoricoPageResponse> listarBancoHorasHistorico(@PathVariable UUID funcionarioId,
                                                                                     @RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size,
                                                                                     @RequestHeader("Authorization") String authorization,
                                                                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = bancoHorasService.listarHistorico(empresaId, funcionarioId, page, size, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/banco-horas/compensacao")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> registrarCompensacao(@Valid @RequestBody BancoHorasCompensacaoRequest request,
                                                     @RequestHeader("Authorization") String authorization,
                                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        bancoHorasService.registrarCompensacao(empresaId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/funcionario/{funcionarioId}/banco-horas/fechamento")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> fechamentoBancoHoras(@PathVariable UUID funcionarioId,
                                                     @Valid @RequestBody FechamentoBancoHorasRequest request,
                                                     @RequestHeader("Authorization") String authorization,
                                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        fechamentoBancoHorasService.fechar(empresaId, funcionarioId, request.anoReferencia(), request.mesReferencia(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
