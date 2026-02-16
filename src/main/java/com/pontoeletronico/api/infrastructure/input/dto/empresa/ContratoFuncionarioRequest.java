package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados do contrato do funcionário")
public record ContratoFuncionarioRequest(
        @Size(max = 50, message = "matricula deve ter no máximo 50 caracteres")
        @Schema(description = "Matrícula (opcional, única)")
        String matricula,
        
        @Size(max = 20, message = "pisPasep deve ter no máximo 20 caracteres")
        @Schema(description = "PIS/PASEP (opcional, único)")
        String pisPasep,
        
        @NotBlank(message = "cargo é obrigatório")
        @Size(min = 2, max = 255, message = "cargo deve ter entre 2 e 255 caracteres")
        @Schema(description = "Cargo", example = "Desenvolvedor", requiredMode = Schema.RequiredMode.REQUIRED)
        String cargo,
        
        @Size(max = 255, message = "departamento deve ter no máximo 255 caracteres")
        @Schema(description = "Departamento")
        String departamento,
        
        @NotNull(message = "tipoContratoId é obrigatório")
        @Schema(description = "ID do tipo de contrato (FK tipo_contrato)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer tipoContratoId,
        
        @Schema(description = "Contrato ativo", example = "true")
        boolean ativo,
        
        @NotNull(message = "dataAdmissao é obrigatória")
        @Schema(description = "Data de admissão (yyyy-MM-dd)", example = "2024-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dataAdmissao,
        
        @Schema(description = "Data de demissão (yyyy-MM-dd)")
        LocalDate dataDemissao,
        
        @NotNull(message = "salarioMensal é obrigatório")
        @DecimalMin(value = "0", message = "salarioMensal deve ser maior ou igual a zero")
        @Schema(description = "Salário mensal", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal salarioMensal,
        
        @NotNull(message = "salarioHora é obrigatório")
        @DecimalMin(value = "0", message = "salarioHora deve ser maior ou igual a zero")
        @Schema(description = "Salário hora", example = "28.41", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal salarioHora
) {}
