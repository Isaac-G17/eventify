package com.eventify.eventify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @NotBlank(message = "La descripción de la categoría no puede estar vacía")
    @Column(nullable = false, length = 250)
    private String descripcion;

    /** Referencia a una categoría existente solo por su id (el servicio la resuelve contra la BD). */
    public static Category conId(Long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
