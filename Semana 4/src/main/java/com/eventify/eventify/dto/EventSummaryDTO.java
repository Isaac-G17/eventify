package com.eventify.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Proyección "plana" para listados masivos: se construye directamente en la consulta JPQL
 * (SELECT new ...), así que nunca se cargan entidades completas ni sus relaciones.
 */
@Schema(description = "Resumen de un evento activo para catálogos y búsquedas")
public record EventSummaryDTO(
        @Schema(description = "ID del evento", example = "21")
        Long id,
        @Schema(description = "Nombre del evento", example = "Concierto de ROCK")
        String nombre,
        @Schema(description = "Fecha del evento", example = "2026-12-20")
        LocalDate fecha,
        @Schema(description = "Nombre del lugar donde se realiza", example = "Estadio Cubierto Los Andes")
        String lugar,
        @Schema(description = "Ciudad del lugar", example = "Bogotá")
        String ciudad
) {
}
