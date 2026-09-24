package com.eventify.eventify.model;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;

    @NotBlank(message = "El nombre del evento no puede estar vacío")
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado")
    private LocalDate fecha;

    @NotBlank(message = "La descripción del evento no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;
}
