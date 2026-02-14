package com.pontoeletronico.api.infrastructure.input.dto.admin;

import com.pontoeletronico.api.domain.enums.TiposCredencial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 51: Criar administrador - Request. */
@Schema(description = "Requisição de cadastro de administrador")
public record AdminCriarRequest(

        @NotBlank(message = "username é obrigatório")
        @Size(max = 255)
        @Pattern(regexp = "^[a-z0-9.-]+$", message = "username não pode conter letras maiúsculas, espaços ou caracteres especiais exceto . e -")
        @Schema(description = "Username único", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @NotBlank(message = "valor é obrigatório")
        @Size(max = 255)
        @Schema(description = "Valor da credencial (email ou username conforme tipoCredencial)", example = "admin@admin.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String valor,

        @NotNull(message = "tipoCredencial é obrigatório")
        @Schema(description = "Tipo da credencial (EMAIL ou USERNAME)", example = "EMAIL", requiredMode = Schema.RequiredMode.REQUIRED)
        TiposCredencial tipoCredencial,

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senha deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Senha de acesso", example = "Admin123!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha
) {}
