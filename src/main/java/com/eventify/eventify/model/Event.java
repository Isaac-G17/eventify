package com.eventify.eventify.model;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
// Filtro global del borrado lógico: ninguna consulta de Hibernate retorna eventos inactivos
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
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

    @Schema(description = "Borrado lógico: al eliminar un evento se marca en false y deja de aparecer en todas las consultas, pero el registro se conserva en la BD para auditoría",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Column(nullable = false)
    private Boolean activo = true;

    @Schema(description = "Lugar donde se realiza el evento (obligatorio). Al registrar basta con enviar su id: {\"id\": 1}")
    @NotNull(message = "El lugar del evento es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Schema(description = "Categorías temáticas del evento. Al registrar basta con enviar sus ids: [{\"id\": 1}, {\"id\": 8}]")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "events_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categorias = new HashSet<>();

    public Event(Long id, String nombre, LocalDate fecha, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }
}
