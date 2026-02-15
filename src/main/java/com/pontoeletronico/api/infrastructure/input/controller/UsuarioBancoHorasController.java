package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.bancohoras.BancoHorasService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.UsuarioBancoHorasSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ResumoBancoHorasResponse;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/usuario")
public class UsuarioBancoHorasController implements UsuarioBancoHorasSwagger {

    private final BancoHorasService bancoHorasService;
    private final JwtUtil jwtUtil;

    public UsuarioBancoHorasController(BancoHorasService bancoHorasService, JwtUtil jwtUtil) {
        this.bancoHorasService = bancoHorasService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/banco-horas/resumo")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<ResumoBancoHorasResponse> resumoBancoHoras(@RequestHeader("Authorization") String authorization,
                                                                    HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        var response = bancoHorasService.resumoBancoHorasPorFuncionario(funcionarioId, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/banco-horas/historico")
    @PreAuthorize("hasAuthority('SCOPE_FUNCIONARIO')")
    public ResponseEntity<BancoHorasHistoricoPageResponse> listarBancoHorasHistorico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var funcionarioId = jwtUtil.extractUserIdFromToken(authorization);
        var response = bancoHorasService.listarHistoricoPorFuncionario(funcionarioId, page, size, httpRequest);
        return ResponseEntity.ok(response);
    }
}
