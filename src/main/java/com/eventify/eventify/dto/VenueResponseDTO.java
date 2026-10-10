package com.eventify.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un lugar")
public record VenueResponseDTO(
        @Schema(description = "ID del lugar", example = "5")
        Long id,
        @Schema(description = "Nombre del lugar", example = "Estadio Cubierto Los Andes")
        String nombre,
        @Schema(description = "Dirección del lugar", example = "Carrera 45 #10-20")
        String direccion,
        @Schema(description = "Ciudad del lugar", example = "Bogotá")
        String ciudad,
        @Schema(description = "Capacidad máxima de personas", example = "1200")
        Integer capacidad
) {
}
