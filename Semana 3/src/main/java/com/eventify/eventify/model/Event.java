package com.eventify.eventify.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del evento no puede estar vacío")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank(message = "La descripción del evento no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;
}
