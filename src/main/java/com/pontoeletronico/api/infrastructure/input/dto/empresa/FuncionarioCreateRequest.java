package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Doc id 12: Cadastro de funcionário - Request. */
@Schema(description = "Requisição de cadastro de funcionário")
public record FuncionarioCreateRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(min = 2, max = 255, message = "username deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[a-z0-9.-]+$", message = "username: apenas letras minúsculas, números, . e -")
        @Schema(description = "Username único do funcionário", example = "joao.silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        
        @NotBlank(message = "nomeCompleto é obrigatório")
        @Size(min = 2, max = 255, message = "nomeCompleto deve ter entre 2 e 255 caracteres")
        @Schema(description = "Nome completo", example = "João da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String nomeCompleto,

        @NotBlank(message = "primeiroNome é obrigatório")
        @Size(min = 2, max = 100, message = "primeiroNome deve ter entre 2 e 100 caracteres")
        @Schema(description = "Primeiro nome", example = "João", requiredMode = Schema.RequiredMode.REQUIRED)
        String primeiroNome,

        @NotBlank(message = "ultimoNome é obrigatório")
        @Size(min = 2, max = 100, message = "ultimoNome deve ter entre 2 e 100 caracteres")
        @Schema(description = "Último nome", example = "Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String ultimoNome,
        
        @NotBlank(message = "cpf é obrigatório")
        @Pattern(regexp = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$", message = "CPF inválido")
        @Schema(description = "CPF (11 dígitos)", example = "123.456.789-00", requiredMode = Schema.RequiredMode.REQUIRED)
        String cpf,
        
        @Schema(description = "Data de nascimento (yyyy-MM-dd)")
        LocalDate dataNascimento,
        
        @NotBlank(message = "email é obrigatório")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "email inválido")
        @Schema(description = "Email (credencial de login)", example = "joao@empresa.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6, message = "senha deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senha deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Senha inicial", example = "Senha123!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha,
        
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
