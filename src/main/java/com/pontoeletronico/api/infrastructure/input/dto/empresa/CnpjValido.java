package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * CNPJ válido pelo Módulo 11 (IN RFB 2.229/2024).
 * Aceita formato numérico (14 dígitos) e alfanumérico (12 A-Z/0-9 + 2 dígitos).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CnpjValidoValidator.class)
@Documented
public @interface CnpjValido {

    String message() default "CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
