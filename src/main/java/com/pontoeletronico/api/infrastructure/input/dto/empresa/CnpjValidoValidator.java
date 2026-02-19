package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.util.CnpjValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidoValidator implements ConstraintValidator<CnpjValido, String> {

    private static final String MSG_FORMATO = "CNPJ deve ter exatamente 14 caracteres (12 alfanuméricos + 2 dígitos verificadores), sem máscara.";
    private static final String MSG_INVALIDO = "CNPJ inválido.";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // obrigatoriedade fica com @NotBlank
        }
        if (!CnpjValidator.isFormatoValido(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MSG_FORMATO).addConstraintViolation();
            return false;
        }
        if (!CnpjValidator.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MSG_INVALIDO).addConstraintViolation();
            return false;
        }
        return true;
    }
}
