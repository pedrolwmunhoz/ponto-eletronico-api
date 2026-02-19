package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import com.pontoeletronico.api.util.CpfValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidoValidator implements ConstraintValidator<CpfValido, String> {

    private static final String MSG_FORMATO = "CPF deve ter exatamente 11 dígitos, sem máscara.";
    private static final String MSG_INVALIDO = "CPF inválido.";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        if (!CpfValidator.isFormatoValido(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MSG_FORMATO).addConstraintViolation();
            return false;
        }
        if (!CpfValidator.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MSG_INVALIDO).addConstraintViolation();
            return false;
        }
        return true;
    }
}
