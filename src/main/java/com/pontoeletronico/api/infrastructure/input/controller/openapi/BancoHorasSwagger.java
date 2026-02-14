package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasCompensacaoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FechamentoBancoHorasRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ResumoBancoHorasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface BancoHorasSwagger {

    @Operation(summary = "Resumo banco de horas", description = "Resumo de banco de horas: totalHorasVencidas, totalHorasEsperadas, totalHorasTrabalhadas, totalFinalBanco (soma mês atual + histórico). Empresa só tem acesso aos funcionários dela mesma.", tags = {"Funcionário (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo: totalHorasVencidas, totalHorasEsperadas, totalHorasTrabalhadas, totalFinalBanco"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ResumoBancoHorasResponse> resumoBancoHoras(@PathVariable("funcionarioId") UUID funcionarioId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Listar histórico banco de horas", description = "Lista histórico mensal de banco de horas (fechamentos aprovados) de um funcionário. JWT empresa + funcionarioId. Mesmo modelo da listagem admin usuarios.", tags = {"Funcionário (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginada de históricos (paginacao + conteudo)"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<BancoHorasHistoricoPageResponse> listarBancoHorasHistorico(@PathVariable("funcionarioId") UUID funcionarioId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Fechamento do mês", description = "Grava o fechamento mensal na tabela banco_horas_historico. JWT empresa + funcionarioId.", tags = {"Funcionário (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fechamento registrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> fechamentoBancoHoras(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody FechamentoBancoHorasRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Registrar compensação", description = "Registra compensação de banco de horas em um histórico. historicoId + minutos. Atualiza valorCompensadoParcial e status pagamento (PARCIAL/PAGO).", tags = {"Funcionário (banco-horas)"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compensação registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Histórico não encontrado ou funcionário não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> registrarCompensacao(@Valid @RequestBody BancoHorasCompensacaoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
