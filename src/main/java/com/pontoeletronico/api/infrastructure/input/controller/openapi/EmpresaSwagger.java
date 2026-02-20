package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.empresa.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EmpresaSwagger {

    @Operation(summary = "Cadastrar empresa", description = "Cria nova empresa com dados fiscais, endereço, telefone e credencial de login (email/senha)", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de validação"),
            @ApiResponse(responseCode = "409", description = "Username, email ou CNPJ já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Map<String, UUID>> criar(@Valid @RequestBody EmpresaCreateRequest request, HttpServletRequest httpRequest);

    @Operation(summary = "Atualizar endereço da empresa", description = "Atualiza endereço da empresa. usuarioId extraído do token JWT.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endereço atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Empresa/endereço não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarEndereco(@Valid @RequestBody EmpresaEnderecoRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Status da configuração inicial", description = "Indica se a empresa já realizou a configuração inicial (jornada padrão). usuarioId extraído do token JWT.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "configInicialRealizada: true se já configurou, false se precisa configurar"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Map<String, Boolean>> configInicialStatus(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Configuração inicial da empresa", description = "Define jornada padrão, banco de horas e geofences (opcional). usuarioId extraído do token JWT.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuração aplicada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> configInicial(@Valid @RequestBody EmpresaConfigInicialRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Resetar senha da empresa", description = "Empresa altera a própria senha (senha antiga + nova). Desativa senha antiga e insere nova. Senha é por usuário, não por credencial. Nunca excluir.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> resetarSenha(@Valid @RequestBody EmpresaResetarSenhaRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Atualizar jornada padrão da empresa", description = "Atualiza jornada padrão da empresa. usuarioId extraído do token JWT.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jornada atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Tipo de escala não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarJornadaPadrao(@Valid @RequestBody EmpresaJornadaConfigRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Atualizar configuração de banco de horas da empresa", description = "Atualiza ativo e totalDiasVencimento da config de banco de horas. Insere registro se a empresa ainda não tiver config. usuarioId da empresa extraído do token JWT.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuração atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarBancoHorasConfig(@Valid @RequestBody EmpresaAtualizarBancoHorasConfigRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Métrica diária da empresa", description = "Retorna o registro de métricas do dia de hoje. Se ainda não houver registro para hoje, retorna o último cadastrado (mais recente).", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica diária"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Nenhuma métrica encontrada para a empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<MetricasDiariaEmpresaResponse> metricasDia(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Listar métricas diárias por período", description = "Retorna lista de métricas diárias da empresa entre dataInicio e dataFim (inclusive). Ordenado por data_ref ASC.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de métricas diárias"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos (dataInicio/dataFim obrigatórios)"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<MetricasDiariaEmpresaResponse>> metricasDiaPorPeriodo(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("dataInicio") LocalDate dataInicio,
            @RequestParam("dataFim") LocalDate dataFim,
            HttpServletRequest httpRequest);

    @Operation(summary = "Atividades recentes", description = "Últimos 4 registros de ponto da empresa (nome do funcionário + horário). Mesmo dado do card da landing. Máximo 4 itens.", tags = {"Empresa"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atividades recentes (nomeCompleto, registradoEm)"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<AtividadeRecenteResponse>> atividadesRecentes(
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest);
}
