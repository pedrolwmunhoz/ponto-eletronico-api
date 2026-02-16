package com.pontoeletronico.api.infrastructure.input.dto.auth;

import com.pontoeletronico.api.domain.enums.TiposCredencial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 1: Login de usuário - Request. */
@LoginRequestValido
@Schema(description = "Requisição de login")
public record LoginRequest(
        
        @NotBlank(message = "valor é obrigatório")
        @Size(min = 2, max = 255, message = "valor deve ter entre 2 e 255 caracteres")
        @Schema(description = "Valor da credencial (email, CPF ou username)", example = "usuario@email.com")
        String valor,
        
        @NotNull(message = "tipoCredencial é obrigatório")
        @Schema(description = "Tipo da credencial", example = "EMAIL")
        TiposCredencial tipoCredencial,
        
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6, message = "senha deve ter no mínimo 6 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\p{Punct}]).*$",
                message = "senha deve conter ao menos uma letra maiúscula e um número ou caractere de pontuação")
        @Schema(description = "Senha do usuário", example = "Senha123!", format = "password")
        String senha
) {}
