package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Duração (ISO-8601) no máximo N horas.
 * Usado para carga diária (12h), carga semanal (44h), tolerância e intervalo (6h).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DurationMaxHoursValidator.class)
@Documented
public @interface DurationMaxHours {

    int value();

    String message() default "Duração deve ser no máximo {value} horas";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
