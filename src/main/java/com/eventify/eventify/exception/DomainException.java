package com.eventify.eventify.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

/**
 * Base de las excepciones de negocio de Eventify. Cada subclase define su código HTTP,
 * su título y su tipo (URI del RFC 7807), así el manejador global las traduce sin casos especiales.
 */
public abstract class DomainException extends RuntimeException {

    private static final String BASE_TIPOS = "https://api.eventify.com/errores/";

    protected DomainException(String mensaje) {
        super(mensaje);
    }

    public abstract HttpStatus getStatus();

    public abstract String getTitulo();

    protected abstract String getCodigo();

    public URI getTipo() {
        return URI.create(BASE_TIPOS + getCodigo());
    }
}
