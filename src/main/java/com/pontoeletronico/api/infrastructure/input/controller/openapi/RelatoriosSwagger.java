package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.domain.enums.FormatoRelatorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface RelatoriosSwagger {

    @Operation(summary = "Relatório ponto detalhado (Doc id 47)", description = "Gera relatório de ponto mensal detalhado (todos os funcionários) em PDF ou Excel. Resposta comprimida com GZIP.", tags = {"Relatórios"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK — arquivo PDF ou Excel (comprimido)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<byte[]> relatorioPontoDetalhado(
            @Parameter(description = "Ano (ex: 2024)") @RequestParam int ano,
            @Parameter(description = "Mês (1-12)") @RequestParam int mes,
            @Parameter(description = "Formato do relatório: PDF ou EXCEL") @RequestParam FormatoRelatorio formato,
            @RequestHeader("Authorization") String authorization);

    @Operation(summary = "Relatório ponto resumo (Doc id 48)", description = "Gera relatório de ponto resumo da empresa em PDF ou Excel. Resposta comprimida com GZIP.", tags = {"Relatórios"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK — arquivo PDF ou Excel (comprimido)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<byte[]> relatorioPontoResumo(
            @Parameter(description = "Ano (ex: 2024)") @RequestParam int ano,
            @Parameter(description = "Mês (1-12)") @RequestParam int mes,
            @Parameter(description = "Formato do relatório: PDF ou EXCEL") @RequestParam FormatoRelatorio formato,
            @RequestHeader("Authorization") String authorization);
}
