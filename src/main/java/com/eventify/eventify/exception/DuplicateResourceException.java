package com.eventify.eventify.exception;

import org.springframework.http.HttpStatus;

/** Intento de crear o renombrar un recurso con un dato que debe ser único (ej. nombre de lugar). */
public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getTitulo() {
        return "Recurso duplicado";
    }

    @Override
    protected String getCodigo() {
        return "recurso-duplicado";
    }
}
