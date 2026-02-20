package com.pontoeletronico.api.infrastructure.input.dto.empresa;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public class DurationMaxHoursValidator implements ConstraintValidator<DurationMaxHours, Duration> {

    private int maxHours;

    @Override
    public void initialize(DurationMaxHours annotation) {
        this.maxHours = annotation.value();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        if (value == null) return true;
        long maxMinutes = maxHours * 60L;
        if (value.toMinutes() > maxMinutes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
