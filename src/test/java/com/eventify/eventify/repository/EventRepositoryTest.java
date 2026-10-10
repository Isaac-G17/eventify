package com.eventify.eventify.repository;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Solo V1 y V2: el esquema real de Flyway pero sin el seed masivo de V3
@DataJpaTest(properties = "spring.flyway.target=2")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    private Venue venue;

    @BeforeEach
    void setUp() {
        venue = venueRepository.save(new Venue(null, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300));
    }

    private Event nuevoEvento(String nombre, LocalDate fecha, String descripcion) {
        Event event = new Event(null, nombre, fecha, descripcion);
        event.setVenue(venue);
        return event;
    }

    @Test
    void save_PersistsEventWithGeneratedId() {
        // Arrange
        Event event = nuevoEvento("Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");

        // Act
        Event saved = eventRepository.save(event);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Conferencia Java", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsMatchingEvents() {
        // Arrange
        eventRepository.save(nuevoEvento("Conferencia Java", LocalDate.now().plusMonths(1), "Descripción 1"));
        eventRepository.save(nuevoEvento("Workshop Spring", LocalDate.now().plusMonths(2), "Descripción 2"));
        eventRepository.save(nuevoEvento("Conferencia Python", LocalDate.now().plusMonths(3), "Descripción 3"));

        // Act
        List<Event> result = eventRepository.findByNombreContaining("Conferencia");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findAll_WithPageable_ReturnsCorrectPageSizeAndMetadata() {
        // Arrange
        for (int i = 1; i <= 12; i++) {
            eventRepository.save(nuevoEvento("Evento " + i, LocalDate.now().plusMonths(1), "Desc " + i));
        }

        // Act
        Page<Event> pagina = eventRepository.findAll(PageRequest.of(0, 5));

        // Assert
        assertEquals(5, pagina.getContent().size());
        assertEquals(12, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
    }
}