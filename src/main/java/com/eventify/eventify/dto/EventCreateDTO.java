package com.eventify.eventify.dto;

import com.eventify.eventify.validation.NoPastEvents;
import com.eventify.eventify.validation.OnCreate;
import com.eventify.eventify.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Datos que el cliente envía para crear (POST) o actualizar (PUT) un evento.
 * Es una clase (no un Record) porque también respalda los formularios Thymeleaf,
 * que necesitan getters/setters para th:field y para conservar lo escrito cuando hay errores.
 */
@Schema(description = "Datos de entrada para registrar o actualizar un evento")
@Getter
@Setter
@NoArgsConstructor
public class EventCreateDTO {

    @Schema(description = "ID del evento. No se envía al crear (POST); es obligatorio al actualizar (PUT) y debe coincidir con el de la URL",
            example = "21", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Null(groups = OnCreate.class, message = "no debe enviarse al crear un evento")
    @NotNull(groups = OnUpdate.class, message = "es obligatorio al actualizar un evento")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Concierto de ROCK", minLength = 3, maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "no debe estar vacío")
    @Size(min = 3, max = 100, message = "debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Schema(description = "Fecha del evento (yyyy-MM-dd). Debe ser futura: no se aceptan fechas pasadas (regla @NoPastEvents) ni el día de hoy (@Future)",
            example = "2026-12-20", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "es obligatoria")
    @Future(message = "debe ser una fecha futura")
    @NoPastEvents
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Schema(description = "Descripción del evento", example = "Gran noche de rock en vivo con bandas nacionales",
            maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "no debe estar vacía")
    @Size(max = 500, message = "no puede superar 500 caracteres")
    private String descripcion;

    @Schema(description = "Correo de contacto del organizador (opcional)", example = "organizador@eventify.com", maxLength = 150)
    @Email(message = "debe ser un correo electrónico válido")
    @Size(max = 150, message = "no puede superar 150 caracteres")
    private String emailOrganizador;

    @Schema(description = "ID del lugar donde se realiza el evento (ver GET /api/venues)", example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "debe seleccionar un lugar")
    private Long venueId;

    @Schema(description = "IDs de las categorías del evento (opcional, ver GET /api/categories)", example = "[1, 8]")
    private Set<Long> categoryIds = new LinkedHashSet<>();
}
