package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ResumoBancoHorasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface UsuarioBancoHorasSwagger {

    @Operation(summary = "Resumo banco de horas (próprio usuário)", description = "Resumo de banco de horas do funcionário logado: totalHorasVencidas, totalHorasEsperadas, totalHorasTrabalhadas, totalFinalBanco. FuncionarioId = userId do JWT.", tags = {"Usuario (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo retornado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não vinculado a empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ResumoBancoHorasResponse> resumoBancoHoras(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Histórico banco de horas (próprio usuário)", description = "Lista paginada do histórico de fechamentos mensais do banco de horas do funcionário logado.", tags = {"Usuario (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginada (paginacao + conteudo)"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não vinculado a empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<BancoHorasHistoricoPageResponse> listarBancoHorasHistorico(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
