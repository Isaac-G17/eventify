package com.eventify.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría temática de eventos")
public record CategoryResponseDTO(
        @Schema(description = "ID de la categoría (se usa en categoryIds al registrar eventos)", example = "8")
        Long id,
        @Schema(description = "Nombre de la categoría", example = "Rock")
        String nombre,
        @Schema(description = "Descripción de la categoría", example = "Eventos del género rock y sus variantes")
        String descripcion
) {
}
