package com.eventify.eventify.repository;

import com.eventify.eventify.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByNombreContaining(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /**
     * Cuenta TODOS los eventos del lugar, incluidos los inactivos (por eso es nativa:
     * así no aplica el filtro @SQLRestriction y se respeta la FK de los eventos borrados lógicamente).
     */
    @Query(value = "SELECT COUNT(*) FROM events WHERE venue_id = :venueId", nativeQuery = true)
    long contarEventosAsociados(@Param("venueId") Long venueId);
}
