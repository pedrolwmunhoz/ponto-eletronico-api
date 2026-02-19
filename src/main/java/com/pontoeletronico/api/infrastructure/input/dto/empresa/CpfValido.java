package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * CPF válido (Módulo 11, dígitos verificadores).
 * Espera 11 dígitos, sem máscara.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CpfValidoValidator.class)
@Documented
public @interface CpfValido {

    String message() default "CPF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
