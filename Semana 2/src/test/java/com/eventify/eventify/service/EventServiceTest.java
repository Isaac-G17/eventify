package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    void findAll_WithPageable_ReturnsPageOfEvents() {
        // Arrange
        List<Event> contenido = List.of(new Event(1L, "Evento 1", LocalDate.of(2026, 10, 10), "Desc 1"));
        Pageable pageable = PageRequest.of(0, 5);
        Page<Event> paginaMock = new PageImpl<>(contenido, pageable, 1);
        when(eventRepository.findAll(pageable)).thenReturn(paginaMock);

        // Act
        Page<Event> result = eventService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_ExistingId_ReturnsEvent() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Descripción");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        Event result = eventService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.findById(99L));
    }

    @Test
    void update_ValidData_ReturnsUpdatedEvent() {
        // Arrange
        Event existente = new Event(1L, "Nombre Viejo", LocalDate.of(2026, 10, 10), "Descripción vieja");
        Event datosNuevos = new Event(null, "Nombre Nuevo", LocalDate.of(2026, 11, 15), "Descripción nueva");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(eventRepository.save(any(Event.class))).thenReturn(existente);

        // Act
        Event result = eventService.update(1L, datosNuevos);

        // Assert
        assertNotNull(result);
        assertEquals("Nombre Nuevo", result.getNombre());
        assertEquals(LocalDate.of(2026, 11, 15), result.getFecha());
        verify(eventRepository, times(1)).save(existente);
    }

    @Test
    void update_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Event datosNuevos = new Event(null, "Nombre Nuevo", LocalDate.of(2026, 11, 15), "Descripción nueva");
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.update(99L, datosNuevos));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void update_InvalidData_ThrowsInvalidDataException() {
        // Arrange
        Event existente = new Event(1L, "Nombre Viejo", LocalDate.of(2026, 10, 10), "Descripción vieja");
        Event datosInvalidos = new Event(null, "", LocalDate.of(2026, 11, 15), "Descripción nueva");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> eventService.update(1L, datosInvalidos));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void delete_ExistingId_DeletesEvent() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Descripción");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        eventService.delete(1L);

        // Assert
        verify(eventRepository, times(1)).delete(existente);
    }

    @Test
    void delete_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.delete(99L));
        verify(eventRepository, never()).delete(any());
    }
}

