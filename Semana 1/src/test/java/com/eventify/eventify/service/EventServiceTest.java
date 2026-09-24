package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "Charla técnica de backend");
    }

    @Test
    void save_ValidEvent_ReturnsSavedEvent() {
        // Arrange (Preparar)
        Event savedMock = new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Charla técnica de backend");
        when(eventRepository.save(validEvent)).thenReturn(savedMock);

        // Act (Actuar)
        Event result = eventService.save(validEvent);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Conferencia Java", result.getNombre());
        verify(eventRepository, times(1)).save(validEvent);
    }

    @Test
    void save_EmptyName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, "   ", LocalDate.of(2026, 10, 10), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_NullName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, null, LocalDate.of(2026, 10, 10), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_NullFecha_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, "Conferencia Java", null, "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_PastFecha_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, "Conferencia Java", LocalDate.of(2020, 1, 1), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_EmptyDescripcion_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), "   ");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_DescripcionTooLong_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        String descripcionLarga = "a".repeat(501);
        Event invalidEvent = new Event(null, "Conferencia Java", LocalDate.of(2026, 10, 10), descripcionLarga);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsListOfEvents() {
        // Arrange (Preparar)
        List<Event> mockList = new ArrayList<>();
        mockList.add(new Event(1L, "Evento 1", LocalDate.of(2026, 10, 10), "Desc 1"));
        when(eventRepository.findAll()).thenReturn(mockList);

        // Act (Actuar)
        List<Event> result = eventService.findAll();

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }
}

