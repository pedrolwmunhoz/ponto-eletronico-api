package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.CpfValido;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Doc id 16: Alterar dados de funcionário - Request. */
@Schema(description = "Requisição de alteração de funcionário. Todos os campos são opcionais; somente os enviados serão atualizados.")
public record FuncionarioUpdateRequest(
        @Size(min = 2, max = 255, message = "username deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[a-z0-9.-]+$", message = "username: apenas letras minúsculas, números, . e -")
        @Schema(description = "Username único do funcionário", example = "joao.silva")
        String username,
        
        @Size(min = 2, max = 255, message = "nomeCompleto deve ter entre 2 e 255 caracteres")
        @Schema(description = "Nome completo", example = "João da Silva")
        String nomeCompleto,

        @Size(min = 2, max = 100, message = "primeiroNome deve ter entre 2 e 100 caracteres")
        @Schema(description = "Primeiro nome (editável). Gravado na tabela.")
        String primeiroNome,

        @Size(min = 2, max = 100, message = "ultimoNome deve ter entre 2 e 100 caracteres")
        @Schema(description = "Último nome (editável). Gravado na tabela.")
        String ultimoNome,
        
        @CpfValido(message = "CPF inválido")
        @Schema(description = "CPF (11 dígitos, sem máscara)", example = "12345678900")
        String cpf,
        
        @Schema(description = "Data de nascimento (yyyy-MM-dd)")
        LocalDate dataNascimento,
        
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "email inválido")
        @Schema(description = "Email (credencial de login)", example = "joao@empresa.com")
        String email,
        
        @Valid
        @Schema(description = "Telefone (opcional)")
        UsuarioTelefoneRequest usuarioTelefone,
        
        @Valid
        @Schema(description = "Contrato (opcional)")
        ContratoFuncionarioRequest contratoFuncionario,
        
        @Valid
        @Schema(description = "Jornada (opcional)")
        JornadaFuncionarioConfigRequest jornadaFuncionarioConfig,
        
        @Schema(description = "IDs dos geofences cadastrados pela empresa aos quais o funcionário terá acesso (opcional). Funcionário não cadastra geofence; apenas associa-se aos geofences já criados pela empresa (xref_geofence_funcionarios).")
        List<UUID> geofenceIds
) {}
