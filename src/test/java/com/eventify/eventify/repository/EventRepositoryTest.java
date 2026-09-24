package com.eventify.eventify.repository;

import com.eventify.eventify.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void save_PersistsEventWithGeneratedId() {
        // Arrange
        Event event = new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "Descripción");

        // Act
        Event saved = eventRepository.save(event);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Conferencia Java", saved.getNombre());
    }

    @Test
    void findByNombreContaining_ReturnsMatchingEvents() {
        // Arrange
        eventRepository.save(new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "Descripción 1"));
        eventRepository.save(new Event(null, "Workshop Spring", LocalDate.of(2026, 11, 10), "Descripción 2"));
        eventRepository.save(new Event(null, "Conferencia Python", LocalDate.of(2026, 12, 10), "Descripción 3"));

        // Act
        List<Event> result = eventRepository.findByNombreContaining("Conferencia");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findAll_WithPageable_ReturnsCorrectPageSizeAndMetadata() {
        // Arrange
        for (int i = 1; i <= 12; i++) {
            eventRepository.save(new Event(null, "Evento " + i, LocalDate.of(2026, 10, 1), "Desc " + i));
        }

        // Act
        Page<Event> pagina = eventRepository.findAll(PageRequest.of(0, 5));

        // Assert
        assertEquals(5, pagina.getContent().size());
        assertEquals(12, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
    }
}