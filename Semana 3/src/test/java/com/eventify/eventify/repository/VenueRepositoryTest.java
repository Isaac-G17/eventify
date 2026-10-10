package com.eventify.eventify.repository;

import com.eventify.eventify.model.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VenueRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void save_PersistsVenueWithGeneratedId() {
        // Arrange
        Venue venue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", 300);

        // Act
        Venue saved = venueRepository.save(venue);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Auditorio Principal", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsMatchingVenues() {
        // Arrange
        venueRepository.save(new Venue(null, "Auditorio Central", "Calle 1", 100));
        venueRepository.save(new Venue(null, "Sala de Juntas", "Calle 2", 20));
        venueRepository.save(new Venue(null, "Auditorio Norte", "Calle 3", 200));

        // Act
        List<Venue> result = venueRepository.findByNombreContaining("Auditorio");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findAll_WithPageable_ReturnsCorrectPageSizeAndMetadata() {
        // Arrange
        for (int i = 1; i <= 12; i++) {
            venueRepository.save(new Venue(null, "Lugar " + i, "Dirección " + i, 50));
        }

        // Act
        Page<Venue> pagina = venueRepository.findAll(PageRequest.of(0, 5));

        // Assert
        assertEquals(5, pagina.getContent().size());
        assertEquals(12, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
    }
}