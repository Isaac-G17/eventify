package com.eventify.eventify.exception;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String mensaje) {
        super(mensaje);
    }
}
