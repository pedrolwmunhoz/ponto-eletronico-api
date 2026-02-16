package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 7: Cadastro de empresa - Request. */
@Schema(description = "Requisição de cadastro de empresa")
public record EmpresaCreateRequest(
        
        @NotBlank(message = "username é obrigatório")
        @Size(min = 2, max = 255, message = "username deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[a-z0-9.-]+$", message = "username: apenas letras minúsculas, números, . e -")
        @Schema(description = "Username único da empresa", example = "empresa-acme", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        
        @NotBlank(message = "email é obrigatório")
        @Size(max = 255, message = "email deve ter no máximo 255 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "email inválido")
        @Schema(description = "Email da empresa (credencial de login)", example = "contato@empresa.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6, message = "senha deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senha deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Senha de acesso", example = "Senha123!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha,
        
        @NotBlank(message = "razaoSocial é obrigatória")
        @Size(min = 2, max = 255, message = "razaoSocial deve ter entre 2 e 255 caracteres")
        @Schema(description = "Razão social", example = "Acme Ltda", requiredMode = Schema.RequiredMode.REQUIRED)
        String razaoSocial,
        
        @NotBlank(message = "cnpj é obrigatório")
        @Pattern(regexp = "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$", message = "CNPJ inválido")
        @Schema(description = "CNPJ (14 dígitos, com ou sem formatação)", example = "12.345.678/0001-90", requiredMode = Schema.RequiredMode.REQUIRED)
        String cnpj,
        
        @NotNull(message = "empresaEndereco é obrigatório")
        @Valid
        @Schema(description = "Endereço da empresa", requiredMode = Schema.RequiredMode.REQUIRED)
        EmpresaEnderecoRequest empresaEndereco,
        
        @NotNull(message = "usuarioTelefone é obrigatório")
        @Valid
        @Schema(description = "Telefone da empresa", requiredMode = Schema.RequiredMode.REQUIRED)
        UsuarioTelefoneRequest usuarioTelefone
) {}
