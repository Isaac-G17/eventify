package com.eventify.eventify.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class NoPastEventsValidator implements ConstraintValidator<NoPastEvents, LocalDate> {

    @Override
    public boolean isValid(LocalDate fecha, ConstraintValidatorContext context) {
        // null lo controla @NotNull: cada anotación valida una sola cosa
        if (fecha == null) {
            return true;
        }
        return !fecha.isBefore(LocalDate.now());
    }
}
