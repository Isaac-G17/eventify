package com.eventify.eventify.exception;

import org.springframework.http.HttpStatus;

/**
 * La petición es válida en formato pero viola una regla de negocio
 * (ej. eliminar un lugar con eventos asociados, o un rango de fechas invertido).
 */
public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }

    @Override
    public String getTitulo() {
        return "Regla de negocio violada";
    }

    @Override
    protected String getCodigo() {
        return "regla-de-negocio";
    }
}
