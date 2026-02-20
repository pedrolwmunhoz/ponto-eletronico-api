package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.empresa.*;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.EmpresaSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.*;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa")
public class EmpresaController implements EmpresaSwagger {

    private final EmpresaCadastroService empresaService;
    private final EmpresaAtualizarEnderecoService empresaAtualizarEnderecoService;
    private final EmpresaConfigInicialService empresaConfigInicialService;
    private final EmpresaResetarSenhaService empresaResetarSenhaService;
    private final EmpresaJornadaPadraoService empresaJornadaPadraoService;
    private final EmpresaBancoHorasConfigService empresaBancoHorasConfigService;
    private final MetricasDiariaEmpresaConsultaService metricasDiariaEmpresaConsultaService;
    private final AtividadeRecenteConsultaService atividadeRecenteConsultaService;
    private final JwtUtil jwtUtil;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    private static final String ACAO_STATUS_CONFIG_INICIAL = "ACESSO_STATUS_CONFIG_INICIAL";

    public EmpresaController(EmpresaCadastroService empresaService,
                             EmpresaAtualizarEnderecoService empresaAtualizarEnderecoService,
                             EmpresaConfigInicialService empresaConfigInicialService,
                             EmpresaResetarSenhaService empresaResetarSenhaService,
                             EmpresaJornadaPadraoService empresaJornadaPadraoService,
                             EmpresaBancoHorasConfigService empresaBancoHorasConfigService,
                             MetricasDiariaEmpresaConsultaService metricasDiariaEmpresaConsultaService,
                             AtividadeRecenteConsultaService atividadeRecenteConsultaService,
                             JwtUtil jwtUtil,
                             AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaService = empresaService;
        this.empresaAtualizarEnderecoService = empresaAtualizarEnderecoService;
        this.empresaConfigInicialService = empresaConfigInicialService;
        this.empresaResetarSenhaService = empresaResetarSenhaService;
        this.empresaJornadaPadraoService = empresaJornadaPadraoService;
        this.empresaBancoHorasConfigService = empresaBancoHorasConfigService;
        this.metricasDiariaEmpresaConsultaService = metricasDiariaEmpresaConsultaService;
        this.atividadeRecenteConsultaService = atividadeRecenteConsultaService;
        this.jwtUtil = jwtUtil;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> criar(@Valid @RequestBody EmpresaCreateRequest request, HttpServletRequest httpRequest) {
        UUID id = empresaService.criar(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/endereco")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> atualizarEndereco(@Valid @RequestBody EmpresaEnderecoRequest request,
                                                  @RequestHeader("Authorization") String authorization,
                                                  HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaAtualizarEnderecoService.atualizar(empresaId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config-inicial/status")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Map<String, Boolean>> configInicialStatus(@RequestHeader("Authorization") String authorization,
                                                                   HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        boolean configInicialRealizada = empresaConfigInicialService.isConfigInicialRealizada(empresaId);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_STATUS_CONFIG_INICIAL, "Consulta status da configuração inicial", null, null, true, null, LocalDateTime.now(), httpRequest);
        return ResponseEntity.ok(Map.of("configInicialRealizada", configInicialRealizada));
    }

    @PostMapping("/config-inicial")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> configInicial(@Valid @RequestBody EmpresaConfigInicialRequest request,
                                              @RequestHeader("Authorization") String authorization,
                                              HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaConfigInicialService.configurar(empresaId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resetar-senha")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> resetarSenha(@Valid @RequestBody EmpresaResetarSenhaRequest request,
                                             @RequestHeader("Authorization") String authorization,
                                             HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaResetarSenhaService.resetar(empresaId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/jornada-padrao")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> atualizarJornadaPadrao(@Valid @RequestBody EmpresaJornadaConfigRequest request,
                                                      @RequestHeader("Authorization") String authorization,
                                                      HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaJornadaPadraoService.atualizar(empresaId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/banco-horas-config")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> atualizarBancoHorasConfig(@Valid @RequestBody EmpresaAtualizarBancoHorasConfigRequest request,
                                                          @RequestHeader("Authorization") String authorization,
                                                          HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        empresaBancoHorasConfigService.atualizar(empresaId, request.empresaBancoHorasConfig(), httpRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metricas-dia")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<MetricasDiariaEmpresaResponse> metricasDia(@RequestHeader("Authorization") String authorization,
                                                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = metricasDiariaEmpresaConsultaService.buscarMetricaHojeOuUltima(empresaId, httpRequest)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Nenhuma métrica diária encontrada para a empresa."));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metricas-dia/por-periodo")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<List<MetricasDiariaEmpresaResponse>> metricasDiaPorPeriodo(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("dataInicio") LocalDate dataInicio,
            @RequestParam("dataFim") LocalDate dataFim,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var list = metricasDiariaEmpresaConsultaService.listarPorDataInicioFim(empresaId, dataInicio, dataFim, httpRequest);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/atividades-recentes")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<List<AtividadeRecenteResponse>> atividadesRecentes(
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var list = atividadeRecenteConsultaService.listar(empresaId, httpRequest);
        return ResponseEntity.ok(list);
    }

}
