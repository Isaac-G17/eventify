package com.eventify.eventify.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venue {
    private Long id;
    private String nombre;
    private String direccion;
    private Integer capacidad;
}
