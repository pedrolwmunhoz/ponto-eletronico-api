package com.pontoeletronico.api.infrastructure.input.controller.openapi;

import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioCreateRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarEmailRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarSenhaRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioUpdateRequest;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.FuncionarioPerfilResponse;
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

import java.util.Map;
import java.util.UUID;

public interface FuncionarioSwagger {

    @Operation(summary = "Cadastrar funcionário", description = "Cria novo funcionário vinculado à empresa. Requer JWT da empresa. Cria users, identificacao_funcionario, user_credential, user_password. Opcionais: usuarioTelefone, contratoFuncionario, jornadaFuncionarioConfig, geofenceIds (IDs dos geofences já cadastrados pela empresa; funcionário não cadastra geofence, apenas associa-se em xref_geofence_funcionarios).", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "409", description = "Username, email ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Map<String, UUID>> cadastrarFuncionario(@Valid @RequestBody FuncionarioCreateRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Desbloquear funcionário", description = "Desativa bloqueio de funcionário da empresa (historico_bloqueio). Nunca exclui.", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Funcionário não pertence à empresa"),
            @ApiResponse(responseCode = "404", description = "Bloqueio não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> desbloquearFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Resetar senha de funcionário", description = "Empresa altera a senha do funcionário. Desativa senha antiga e insere nova. Senha é por usuário, não por credencial.", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> resetarSenhaFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody FuncionarioResetarSenhaRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Resetar email de funcionário", description = "Empresa altera o email do funcionário.", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> resetarEmailFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody FuncionarioResetarEmailRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Alterar funcionário", description = "Atualiza dados do funcionário. Mesmo corpo do cadastro (sem senha).", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "409", description = "Username ou email já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> atualizarFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @Valid @RequestBody FuncionarioUpdateRequest request, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Deletar funcionário", description = "Soft delete do funcionário (ativo=false, dataDesativacao=now).", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário deletado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado ou não pertence à empresa"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Listar funcionários", description = "Lista funcionários da empresa com paginação e filtro por nome.", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de funcionários"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FuncionarioListagemPageResponse> listarFuncionarios(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize, @RequestParam(required = false) String nome, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);

    @Operation(summary = "Recuperar perfil do funcionário", description = "Recuperar perfil do funcionário. usuarioId extraído do token JWT no backend.", tags = {"Funcionário"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil do funcionário"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<FuncionarioPerfilResponse> getPerfilFuncionario(@PathVariable("funcionarioId") UUID funcionarioId, @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest);
}
