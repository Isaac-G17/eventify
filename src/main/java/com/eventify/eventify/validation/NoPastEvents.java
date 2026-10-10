package com.eventify.eventify.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Regla de negocio: no se registran eventos con fecha anterior a hoy.
 * A diferencia de @Future (regla genérica de formato), su mensaje explica la política del negocio.
 */
@Documented
@Constraint(validatedBy = NoPastEventsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPastEvents {

    String message() default "Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
