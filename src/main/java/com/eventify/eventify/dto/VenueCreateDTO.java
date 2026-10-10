package com.eventify.eventify.dto;

import com.eventify.eventify.validation.OnCreate;
import com.eventify.eventify.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos que el cliente envía para crear (POST) o actualizar (PUT) un lugar.
 * Clase con getters/setters para poder usarse también como th:object en Thymeleaf.
 */
@Schema(description = "Datos de entrada para registrar o actualizar un lugar")
@Getter
@Setter
@NoArgsConstructor
public class VenueCreateDTO {

    @Schema(description = "ID del lugar. No se envía al crear (POST); es obligatorio al actualizar (PUT) y debe coincidir con el de la URL",
            example = "1", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Null(groups = OnCreate.class, message = "no debe enviarse al crear un lugar")
    @NotNull(groups = OnUpdate.class, message = "es obligatorio al actualizar un lugar")
    private Long id;

    @Schema(description = "Nombre del lugar (único)", example = "Estadio Cubierto Los Andes", minLength = 3, maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "no debe estar vacío")
    @Size(min = 3, max = 150, message = "debe tener entre 3 y 150 caracteres")
    private String nombre;

    @Schema(description = "Dirección del lugar", example = "Carrera 45 #10-20", maxLength = 250,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "no debe estar vacía")
    @Size(max = 250, message = "no puede superar 250 caracteres")
    private String direccion;

    @Schema(description = "Ciudad del lugar", example = "Bogotá", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "no debe estar vacía")
    @Size(max = 100, message = "no puede superar 100 caracteres")
    private String ciudad;

    @Schema(description = "Capacidad máxima de personas", example = "1200", minimum = "10", maximum = "100000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "es obligatoria")
    @Min(value = 10, message = "debe ser de al menos 10 personas")
    @Max(value = 100000, message = "no puede superar 100000 personas")
    private Integer capacidad;
}
