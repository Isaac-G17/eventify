package com.eventify.eventify.service;

import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventResponseDTO;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.mapper.EventMapperImpl;
import com.eventify.eventify.mapper.ReferenceMapper;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.CategoryRepository;
import com.eventify.eventify.repository.EventRepository;
import com.eventify.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Lógica del servicio con el mapper real de MapStruct (EventMapperImpl + ReferenceMapper) y repositorios simulados.
 * Las validaciones de formato (nombre vacío, fecha pasada...) están en EventCreateDTOValidationTest.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private EventService eventService;

    private EventCreateDTO validEvent;

    private Venue venue;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, new EventMapperImpl(new ReferenceMapper(venueRepository, categoryRepository)));
        venue = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
        validEvent = eventDto(null, "Conferencia Java", LocalDate.now().plusMonths(1), "Charla técnica de backend", 1L);
    }

    @Test
    void save_ValidEvent_ReturnsResponseWithResolvedNames() {
        // Arrange (Preparar)
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        // Act (Actuar)
        EventResponseDTO result = eventService.save(validEvent);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Conferencia Java", result.nombre());
        assertEquals("Auditorio Principal", result.venueName());
        assertEquals("Bogotá", result.venueCity());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void save_WithCategoryIds_ResolvesEntitiesAndReturnsSortedNames() {
        // Escenario 6: venueId=1 y categoryIds=[1,3,5] -> entidades resueltas y nombres en la respuesta
        Category conciertos = new Category(1L, "Conciertos", "Música en vivo");
        Category conferencias = new Category(3L, "Conferencias", "Charlas");
        Category gastronomia = new Category(5L, "Gastronomía", "Comida");
        validEvent.setCategoryIds(new LinkedHashSet<>(List.of(1L, 3L, 5L)));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(categoryRepository.findAllById(Set.of(1L, 3L, 5L))).thenReturn(List.of(gastronomia, conciertos, conferencias));
        ArgumentCaptor<Event> guardado = ArgumentCaptor.forClass(Event.class);
        when(eventRepository.save(guardado.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        EventResponseDTO result = eventService.save(validEvent);

        // Assert
        assertSame(venue, guardado.getValue().getVenue());
        assertEquals(Set.of(conciertos, conferencias, gastronomia), guardado.getValue().getCategorias());
        assertTrue(guardado.getValue().getActivo());
        assertEquals(List.of("Conciertos", "Conferencias", "Gastronomía"), result.categoryNames());
    }

    @Test
    void save_NonExistingVenue_ThrowsResourceNotFoundException() {
        // Arrange
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> eventService.save(validEvent));
        assertEquals("El lugar con ID 1 no existe", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_NonExistingCategory_ThrowsResourceNotFoundExceptionWithMissingIds() {
        // Arrange
        validEvent.setCategoryIds(Set.of(1L, 99L));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(categoryRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(new Category(1L, "Conciertos", "Música")));

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> eventService.save(validEvent));
        assertEquals("Las categorías con ID [99] no existen", ex.getMessage());
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
    void buscar_FechaDesdeAfterFechaHasta_ThrowsBusinessRuleViolationException() {
        // Arrange
        EventFilter filtro = new EventFilter(null, null, null, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> eventService.buscar(filtro, PageRequest.of(0, 5)));
        verify(eventRepository, never()).buscarResumen(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findById_ExistingId_ReturnsFlatResponse() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");
        existente.setVenue(venue);
        existente.getCategorias().add(new Category(8L, "Rock", "Género rock"));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        EventResponseDTO result = eventService.findById(1L);

        // Assert
        assertEquals(1L, result.id());
        assertEquals("Auditorio Principal", result.venueName());
        assertEquals(List.of("Rock"), result.categoryNames());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(eventRepository.findById(9999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> eventService.findById(9999L));
        assertEquals("El evento con ID 9999 no existe", ex.getMessage());
    }

    @Test
    void findFormById_ReturnsIdsForEditForm() {
        // Arrange
        Event existente = new Event(1L, "Conferencia Java", LocalDate.now().plusMonths(1), "Descripción");
        existente.setVenue(venue);
        existente.getCategorias().add(new Category(8L, "Rock", "Género rock"));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        EventCreateDTO form = eventService.findFormById(1L);

        // Assert
        assertEquals(1L, form.getId());
        assertEquals(1L, form.getVenueId());
        assertEquals(Set.of(8L), form.getCategoryIds());
    }

    @Test
    void update_ValidData_ReturnsUpdatedEvent() {
        // Arrange
        Event existente = new Event(1L, "Nombre Viejo", LocalDate.now().plusMonths(1), "Descripción vieja");
        existente.setVenue(new Venue(2L, "Otro lugar", "Calle", "Cali", 100));
        EventCreateDTO datosNuevos = eventDto(1L, "Nombre Nuevo", LocalDate.now().plusMonths(2), "Descripción nueva", 1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(eventRepository.save(existente)).thenReturn(existente);

        // Act
        EventResponseDTO result = eventService.update(1L, datosNuevos);

        // Assert
        assertEquals("Nombre Nuevo", result.nombre());
        assertEquals(LocalDate.now().plusMonths(2), result.fecha());
        assertEquals("Auditorio Principal", result.venueName());
        assertEquals(1L, existente.getId(), "El id de la entidad nunca cambia");
        verify(eventRepository, times(1)).save(existente);
    }

    @Test
    void update_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        EventCreateDTO datosNuevos = eventDto(99L, "Nombre Nuevo", LocalDate.now().plusMonths(2), "Descripción nueva", 1L);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventService.update(99L, datosNuevos));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void update_IdMismatch_ThrowsBusinessRuleViolationException() {
        // Arrange
        EventCreateDTO datosNuevos = eventDto(2L, "Nombre Nuevo", LocalDate.now().plusMonths(2), "Descripción nueva", 1L);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> eventService.update(1L, datosNuevos));
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

    static EventCreateDTO eventDto(Long id, String nombre, LocalDate fecha, String descripcion, Long venueId) {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setId(id);
        dto.setNombre(nombre);
        dto.setFecha(fecha);
        dto.setDescripcion(descripcion);
        dto.setVenueId(venueId);
        return dto;
    }
}
