package com.eventify.eventify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus: en el panel (vistas) Spring responde 404 en lugar de 500 si no se captura
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getTitulo() {
        return "Recurso no encontrado";
    }

    @Override
    protected String getCodigo() {
        return "recurso-no-encontrado";
    }
}
