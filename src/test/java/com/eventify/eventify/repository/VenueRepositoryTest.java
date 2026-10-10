package com.eventify.eventify.repository;

import com.eventify.eventify.model.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Esquema real de Flyway (V1-V4) sin el seed masivo: se vacía dentro de la transacción del test, que se revierte al final
@DataJpaTest
@Sql(statements = {"DELETE FROM events_categories", "DELETE FROM events", "DELETE FROM venues"})
class VenueRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void save_PersistsVenueWithGeneratedId() {
        // Arrange
        Venue venue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);

        // Act
        Venue saved = venueRepository.save(venue);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Auditorio Principal", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsMatchingVenues() {
        // Arrange
        venueRepository.save(new Venue(null, "Auditorio Central", "Calle 1", "Bogotá", 100));
        venueRepository.save(new Venue(null, "Sala de Juntas", "Calle 2", "Bogotá", 20));
        venueRepository.save(new Venue(null, "Auditorio Norte", "Calle 3", "Bogotá", 200));

        // Act
        List<Venue> result = venueRepository.findByNombreContaining("Auditorio");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findAll_WithPageable_ReturnsCorrectPageSizeAndMetadata() {
        // Arrange
        for (int i = 1; i <= 12; i++) {
            venueRepository.save(new Venue(null, "Lugar " + i, "Dirección " + i, "Bogotá", 50));
        }

        // Act
        Page<Venue> pagina = venueRepository.findAll(PageRequest.of(0, 5));

        // Assert
        assertEquals(5, pagina.getContent().size());
        assertEquals(12, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
    }

    @Test
    void existsByNombreIgnoreCase_DetectaDuplicadosSinImportarMayusculas() {
        // Arrange
        Venue guardado = venueRepository.save(new Venue(null, "Teatro Municipal", "Plaza Central 12", "Cali", 300));

        // Act & Assert
        assertTrue(venueRepository.existsByNombreIgnoreCase("TEATRO municipal"));
        assertFalse(venueRepository.existsByNombreIgnoreCase("Teatro Nacional"));
        assertFalse(venueRepository.existsByNombreIgnoreCaseAndIdNot("teatro municipal", guardado.getId()));
    }
}
