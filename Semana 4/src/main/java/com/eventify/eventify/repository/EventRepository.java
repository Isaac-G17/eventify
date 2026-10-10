package com.eventify.eventify.repository;

import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByNombreContaining(String nombre);

    /**
     * Detalle de un evento con su lugar y categorías en una sola consulta (evita N+1).
     * Gracias a @SQLRestriction, un evento inactivo no se encuentra.
     */
    @Override
    @EntityGraph(attributePaths = {"venue", "categorias"})
    Optional<Event> findById(Long id);

    /**
     * Motor de búsqueda del catálogo: todos los filtros son opcionales (null = no filtra) y se combinan con AND.
     * Ciudad y categoría usan búsqueda parcial e insensible a mayúsculas.
     * Retorna un Slice de Records construido en la misma consulta (un solo SELECT con JOIN, sin COUNT).
     */
    @Query("""
            SELECT new com.eventify.eventify.dto.EventSummaryDTO(e.id, e.nombre, e.fecha, v.nombre, v.ciudad)
            FROM Event e
            JOIN e.venue v
            WHERE (:ciudad IS NULL OR LOWER(v.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
              AND (:categoria IS NULL OR EXISTS (
                    SELECT c.id FROM e.categorias c
                    WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :categoria, '%'))))
              AND (:capacidadMinima IS NULL OR v.capacidad >= :capacidadMinima)
              AND (:fechaDesde IS NULL OR e.fecha >= :fechaDesde)
              AND (:fechaHasta IS NULL OR e.fecha <= :fechaHasta)
            ORDER BY e.fecha DESC, e.id DESC
            """)
    Slice<EventSummaryDTO> buscarResumen(@Param("ciudad") String ciudad,
                                         @Param("categoria") String categoria,
                                         @Param("capacidadMinima") Integer capacidadMinima,
                                         @Param("fechaDesde") LocalDate fechaDesde,
                                         @Param("fechaHasta") LocalDate fechaHasta,
                                         Pageable pageable);

    /**
     * Borrado lógico: en lugar de DELETE se actualiza el estado.
     * Se usa un UPDATE directo para no disparar las validaciones de la entidad
     * (ej. @FutureOrPresent bloquearía desactivar un evento que ya pasó).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.activo = false WHERE e.id = :id")
    void softDelete(@Param("id") Long id);
}
