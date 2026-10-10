package com.eventify.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Detalle de un evento que se devuelve al cliente. Las relaciones llegan "aplanadas"
 * como texto (venueName, categoryNames): no se exponen IDs internos ni la estructura JPA.
 */
@Schema(description = "Detalle de un evento activo")
public record EventResponseDTO(
        @Schema(description = "ID del evento", example = "21")
        Long id,
        @Schema(description = "Nombre del evento", example = "Concierto de ROCK")
        String nombre,
        @Schema(description = "Fecha del evento", example = "2026-12-20")
        LocalDate fecha,
        @Schema(description = "Descripción del evento", example = "Gran noche de rock en vivo con bandas nacionales")
        String descripcion,
        @Schema(description = "Correo del organizador (puede ser null)", example = "organizador@eventify.com")
        String emailOrganizador,
        @Schema(description = "Nombre del lugar", example = "Estadio Cubierto Los Andes")
        String venueName,
        @Schema(description = "Ciudad del lugar", example = "Bogotá")
        String venueCity,
        @Schema(description = "Nombres de las categorías, en orden alfabético", example = "[\"Conciertos\", \"Rock\"]")
        List<String> categoryNames
) {
}
