package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Telefone do usuário")
public record UsuarioTelefoneRequest(
        @NotBlank(message = "codigoPais é obrigatório")
        @Size(max = 10, message = "codigoPais deve ter no máximo 10 caracteres")
        @Pattern(regexp = "^[0-9]+$", message = "codigoPais: apenas números")
        @Schema(description = "Código do país", example = "55", requiredMode = Schema.RequiredMode.REQUIRED)
        String codigoPais,
        
        @NotBlank(message = "ddd é obrigatório")
        @Size(max = 5, message = "ddd deve ter no máximo 5 caracteres")
        @Pattern(regexp = "^[0-9]+$", message = "ddd: apenas números")
        @Schema(description = "DDD", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
        String ddd,
        
        @NotBlank(message = "numero é obrigatório")
        @Size(max = 20, message = "numero deve ter no máximo 20 caracteres")
        @Pattern(regexp = "^[0-9]+$", message = "numero: apenas números")
        @Schema(description = "Número", example = "999999999", requiredMode = Schema.RequiredMode.REQUIRED)
        String numero
) {}
