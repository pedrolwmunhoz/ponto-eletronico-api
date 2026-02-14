package com.pontoeletronico.api.infrastructure.input.dto.auth;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoginRequestValidoValidator.class)
@Documented
public @interface LoginRequestValido {

    String message() default "valor não é válido para o tipo de credencial informado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
