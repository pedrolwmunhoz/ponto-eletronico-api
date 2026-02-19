package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Doc id 8: Atualizar endereço da empresa - Request. */
@Schema(description = "Endereço da empresa")
public record EmpresaEnderecoRequest(
        
        @NotBlank(message = "rua é obrigatória")
        @Size(min = 2, max = 255, message = "rua deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9\\s\\p{Punct}]+$", message = "rua: apenas letras, números, espaços e pontuação")
        @Schema(description = "Rua", example = "Av. Paulista", requiredMode = Schema.RequiredMode.REQUIRED)
        String rua,
        
        @NotBlank(message = "numero é obrigatório")
        @Size(min = 1, max = 4, message = "numero deve ter entre 1 e 4 dígitos")
        @Pattern(regexp = "^[0-9]{1,4}$", message = "numero: apenas números (1 a 4 dígitos)")
        @Schema(description = "Número (1 a 4 dígitos, sem máscara)", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
        String numero,
        
        @Size(max = 255, message = "complemento deve ter no máximo 255 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9\\s\\p{Punct}]*$", message = "complemento: apenas letras, números, espaços e pontuação")
        @Schema(description = "Complemento")
        String complemento,
        
        @NotBlank(message = "bairro é obrigatório")
        @Size(min = 2, max = 255, message = "bairro deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9\\s\\p{Punct}]+$", message = "bairro: apenas letras, números, espaços e pontuação")
        @Schema(description = "Bairro", example = "Bela Vista", requiredMode = Schema.RequiredMode.REQUIRED)
        String bairro,
        
        @NotBlank(message = "cidade é obrigatória")
        @Size(min = 2, max = 255, message = "cidade deve ter entre 2 e 255 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9\\s\\p{Punct}]+$", message = "cidade: apenas letras, números, espaços e pontuação")
        @Schema(description = "Cidade", example = "São Paulo", requiredMode = Schema.RequiredMode.REQUIRED)
        String cidade,
        
        @NotBlank(message = "uf é obrigatória")
        @Size(min = 2, max = 2, message = "uf deve ter 2 caracteres")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "uf: 2 letras (ex: SP)")
        @Schema(description = "UF (2 caracteres)", example = "SP", requiredMode = Schema.RequiredMode.REQUIRED)
        String uf,
        
        @NotBlank(message = "cep é obrigatório")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$|^\\d{8}$", message = "CEP inválido (8 dígitos)")
        @Schema(description = "CEP (8 dígitos)", example = "01310100", requiredMode = Schema.RequiredMode.REQUIRED)
        String cep
) {}
