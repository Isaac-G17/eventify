package com.eventify.eventify.service;

import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueService venueService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private EventService eventService;

    private Event validEvent;

    private Venue venue;

    @BeforeEach
    void setUp() {
        validEvent = new Event(null, "Conferencia Java", LocalDate.now().plusMonths(1), "Charla técnica de backend");
        venue = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
        validEvent.setVenue(venue);
    }

    @Test
    void save_ValidEvent_ReturnsSavedEvent() {
        // Arrange (Preparar)
        Event savedMock = new Event(1L, "Conferencia Java", LocalDate.now().plusMonths(1), "Charla técnica de backend");
        when(venueService.findById(1L)).thenReturn(venue);
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
        Event invalidEvent = new Event(null, "   ", LocalDate.now().plusMonths(1), "Descripción");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_NullName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Event invalidEvent = new Event(null, null, LocalDate.now().plusMonths(1), "Descripción");

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
        Event invalidEvent = new Event(null, "Conferencia Java", LocalDate.now().plusMonths(1), "   ");

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_DescripcionTooLong_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        String descripcionLarga = "a".repeat(501);
        Event invalidEvent = new Event(null, "Conferencia Java", LocalDate.now().plusMonths(1), descripcionLarga);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> eventService.save(invalidEvent));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void buscar_WithFilters_ReturnsSliceOfSummaries() {
        // Arrange
        EventFilter filtro = new EventFilter("bog", "rock", null, null, null);
        Pageable pageable = PageRequest.of(0, 5);
        List<EventSummaryDTO> contenido = List.of(new EventSummaryDTO(1L, "Concierto de ROCK", LocalDate.now().plusMonths(1), "Estadio", "Bogotá"));
        Slice<EventSummaryDTO> sliceMock = new SliceImpl<>(contenido, pageable, false);
        when(eventRepository.buscarResumen("bog", "rock", null, null, null, pageable)).thenReturn(sliceMock);

        // Act
        Slice<EventSummaryDTO> result = eventService.buscar(filtro, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertFalse(result.hasNext());
        verify(eventRepository, times(1)).buscarResumen("bog", "rock", null, null, null, pageable);
    }

    @Test
    void buscar_FechaDesdeAfterFechaHasta_ThrowsInvalidDataException() {
        // Arrange
        EventFilter filtro = new EventFilter(null, null, null, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> eventService.buscar(filtro, PageRequest.of(0, 5)));
        verify(eventRepository, never()).buscarResumen(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findById_ExistingId_ReturnsEvent() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");
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
        Event existente = new Event(1L, "Nombre Viejo", LocalDate.now().plusMonths(1), "Descripción vieja");
        Event datosNuevos = new Event(null, "Nombre Nuevo", LocalDate.now().plusMonths(2), "Descripción nueva");
        datosNuevos.setVenue(venue);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueService.findById(1L)).thenReturn(venue);
        when(eventRepository.save(any(Event.class))).thenReturn(existente);

        // Act
        Event result = eventService.update(1L, datosNuevos);

        // Assert
        assertNotNull(result);
        assertEquals("Nombre Nuevo", result.getNombre());
        assertEquals(LocalDate.now().plusMonths(2), result.getFecha());
        assertEquals("Auditorio Principal", result.getVenue().getNombre());
        verify(eventRepository, times(1)).save(existente);
    }

    @Test
    void update_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Event datosNuevos = new Event(null, "Nombre Nuevo", LocalDate.now().plusMonths(2), "Descripción nueva");
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.update(99L, datosNuevos));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void update_InvalidData_ThrowsInvalidDataException() {
        // Arrange
        Event existente = new Event(1L, "Nombre Viejo", LocalDate.now().plusMonths(1), "Descripción vieja");
        Event datosInvalidos = new Event(null, "", LocalDate.now().plusMonths(2), "Descripción nueva");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> eventService.update(1L, datosInvalidos));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void delete_ExistingId_SoftDeletesEvent() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        eventService.delete(1L);

        // Assert
        verify(eventRepository, times(1)).softDelete(1L);
        verify(eventRepository, never()).delete(any());
    }

    @Test
    void delete_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.delete(99L));
        verify(eventRepository, never()).softDelete(any());
    }

    @Test
    void save_WithoutVenue_ThrowsInvalidDataException() {
        // Arrange
        Event sinLugar = new Event(null, "Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> eventService.save(sinLugar));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_WithCategories_ResolvesRelationsAndForcesActive() {
        // Arrange
        Category conciertos = new Category(1L, "Conciertos", "Música en vivo");
        Category rock = new Category(8L, "Rock", "Género rock");
        validEvent.getCategorias().add(Category.conId(1L));
        validEvent.getCategorias().add(Category.conId(8L));
        validEvent.setActivo(false);
        when(venueService.findById(1L)).thenReturn(venue);
        when(categoryService.findAllByIds(Set.of(1L, 8L))).thenReturn(List.of(conciertos, rock));
        when(eventRepository.save(validEvent)).thenReturn(validEvent);

        // Act
        Event result = eventService.save(validEvent);

        // Assert
        assertSame(venue, result.getVenue());
        assertEquals(Set.of(conciertos, rock), result.getCategorias());
        assertTrue(result.getActivo());
    }
}

