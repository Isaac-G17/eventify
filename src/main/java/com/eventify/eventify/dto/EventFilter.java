package com.eventify.eventify.dto;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filtros opcionales del catálogo. Todos se combinan con AND; los que llegan vacíos se ignoran.
 */
public record EventFilter(
        @Parameter(description = "Ciudad del lugar. Búsqueda parcial e insensible a mayúsculas (ej: 'bog' encuentra 'Bogotá')", example = "bog")
        String ciudad,

        @Parameter(description = "Nombre de una categoría asociada al evento. Búsqueda parcial e insensible a mayúsculas (ej: 'rock' encuentra 'Rock')", example = "rock")
        String categoria,

        @Parameter(description = "Capacidad mínima del lugar del evento", example = "300")
        Integer capacidadMinima,

        @Parameter(description = "Fecha inicial del rango (inclusive), formato yyyy-MM-dd", example = "2026-10-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaDesde,

        @Parameter(description = "Fecha final del rango (inclusive), formato yyyy-MM-dd", example = "2026-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaHasta
) {

    public EventFilter {
        ciudad = limpiar(ciudad);
        categoria = limpiar(categoria);
    }

    public static EventFilter sinFiltros() {
        return new EventFilter(null, null, null, null, null);
    }

    public boolean hayFiltrosActivos() {
        return ciudad != null || categoria != null || capacidadMinima != null || fechaDesde != null || fechaHasta != null;
    }

    private static String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
