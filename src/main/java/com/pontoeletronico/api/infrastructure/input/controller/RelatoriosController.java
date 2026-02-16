package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.enums.FormatoRelatorio;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.relatorio.RelatorioExportService;
import com.pontoeletronico.api.domain.services.relatorio.RelatorioPontoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.RelatoriosSwagger;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa/relatorios")
public class RelatoriosController implements RelatoriosSwagger {

    private static final String ACAO_RELATORIO_PONTO_DETALHADO = "GERAR_RELATORIO_PONTO_DETALHADO";
    private static final String ACAO_RELATORIO_PONTO_RESUMO = "GERAR_RELATORIO_PONTO_RESUMO";

    private final RelatorioPontoService relatorioPontoService;
    private final RelatorioExportService relatorioExportService;
    private final JwtUtil jwtUtil;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public RelatoriosController(RelatorioPontoService relatorioPontoService,
                                RelatorioExportService relatorioExportService,
                                JwtUtil jwtUtil,
                                AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.relatorioPontoService = relatorioPontoService;
        this.relatorioExportService = relatorioExportService;
        this.jwtUtil = jwtUtil;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 47: Gerar relatório de ponto mensal detalhado (PDF ou Excel, comprimido). */
    @PostMapping("/ponto-detalhado")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<byte[]> relatorioPontoDetalhado(@RequestParam int ano,
                                                          @RequestParam int mes,
                                                          @RequestParam FormatoRelatorio formato,
                                                          @RequestHeader("Authorization") String authorization,
                                                          HttpServletRequest httpRequest) throws IOException {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var dados = relatorioPontoService.gerarDadosDetalhado(empresaId, ano, mes);
        var bytes = relatorioExportService.exportarDetalhado(dados, formato);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RELATORIO_PONTO_DETALHADO, "Geração de relatório de ponto detalhado", null, null, true, null, LocalDateTime.now(), httpRequest);
        return respostaArquivo(bytes, formato, "ponto-detalhado", ano, mes);
    }

    /** Doc id 48: Gerar relatório de ponto resumo (PDF ou Excel, comprimido). */
    @PostMapping("/ponto-resumo")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<byte[]> relatorioPontoResumo(@RequestParam int ano,
                                                        @RequestParam int mes,
                                                        @RequestParam FormatoRelatorio formato,
                                                        @RequestHeader("Authorization") String authorization,
                                                        HttpServletRequest httpRequest) throws IOException {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var dados = relatorioPontoService.gerarDadosResumo(empresaId, ano, mes);
        var bytes = relatorioExportService.exportarResumo(dados, formato);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RELATORIO_PONTO_RESUMO, "Geração de relatório de ponto resumo", null, null, true, null, LocalDateTime.now(), httpRequest);
        return respostaArquivo(bytes, formato, "ponto-resumo", ano, mes);
    }

    private ResponseEntity<byte[]> respostaArquivo(byte[] bytesGzip, FormatoRelatorio formato, String nomeBase, int ano, int mes) {
        var ext = relatorioExportService.extensaoArquivo(formato);
        var filename = String.format("%s-%04d-%02d.%s", nomeBase, ano, mes, ext);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(relatorioExportService.contentType(formato)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .body(bytesGzip);
    }
}
