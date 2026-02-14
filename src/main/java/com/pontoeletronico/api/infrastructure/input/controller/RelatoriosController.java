package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.enums.FormatoRelatorio;
import com.pontoeletronico.api.domain.services.relatorio.RelatorioExportService;
import com.pontoeletronico.api.domain.services.relatorio.RelatorioPontoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.RelatoriosSwagger;
import com.pontoeletronico.api.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/empresa/relatorios")
public class RelatoriosController implements RelatoriosSwagger {

    private final RelatorioPontoService relatorioPontoService;
    private final RelatorioExportService relatorioExportService;
    private final JwtUtil jwtUtil;

    public RelatoriosController(RelatorioPontoService relatorioPontoService,
                                RelatorioExportService relatorioExportService,
                                JwtUtil jwtUtil) {
        this.relatorioPontoService = relatorioPontoService;
        this.relatorioExportService = relatorioExportService;
        this.jwtUtil = jwtUtil;
    }

    /** Doc id 47: Gerar relatório de ponto mensal detalhado (PDF ou Excel, comprimido). */
    @PostMapping("/ponto-detalhado")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<byte[]> relatorioPontoDetalhado(@RequestParam int ano,
                                                          @RequestParam int mes,
                                                          @RequestParam FormatoRelatorio formato,
                                                          @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var dados = relatorioPontoService.gerarDadosDetalhado(empresaId, ano, mes);
        try {
            var bytes = relatorioExportService.exportarDetalhado(dados, formato);
            return respostaArquivo(bytes, formato, "ponto-detalhado", ano, mes);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
    }

    /** Doc id 48: Gerar relatório de ponto resumo (PDF ou Excel, comprimido). */
    @PostMapping("/ponto-resumo")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<byte[]> relatorioPontoResumo(@RequestParam int ano,
                                                        @RequestParam int mes,
                                                        @RequestParam FormatoRelatorio formato,
                                                        @RequestHeader("Authorization") String authorization) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var dados = relatorioPontoService.gerarDadosResumo(empresaId, ano, mes);
        try {
            var bytes = relatorioExportService.exportarResumo(dados, formato);
            return respostaArquivo(bytes, formato, "ponto-resumo", ano, mes);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
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
