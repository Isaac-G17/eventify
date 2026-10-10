package com.eventify.eventify.service;


import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.VenueRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    private Venue validVenue;

    @BeforeEach
    void setUp() {
        validVenue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", 300);
    }

    @Test
    void save_ValidVenue_ReturnsSavedVenue() {
        // Arrange (Preparar)
        Venue savedMock = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", 300);
        when(venueRepository.save(validVenue)).thenReturn(savedMock);

        // Act (Actuar)
        Venue result = venueService.save(validVenue);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Auditorio Principal", result.getNombre());
        verify(venueRepository, times(1)).save(validVenue);
    }

    @Test
    void save_EmptyName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "", "Calle 50 #20-10", 300);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void save_NullName_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, null, "Calle 50 #20-10", 300);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void save_EmptyDireccion_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "Auditorio Principal", "   ", 300);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void save_NullCapacidad_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", null);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void save_NegativeCapacidad_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", -5);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void save_ZeroCapacidad_ThrowsInvalidDataException() {
        // Arrange (Preparar)
        Venue invalidVenue = new Venue(null, "Auditorio Principal", "Calle 50 #20-10", 0);

        // Act & Assert (Actuar y Verificar)
        assertThrows(InvalidDataException.class, () -> venueService.save(invalidVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void findAll_WithPageable_ReturnsPageOfVenues() {
        // Arrange
        List<Venue> contenido = List.of(new Venue(1L, "Lugar A", "Dirección A", 100));
        Pageable pageable = PageRequest.of(0, 5);
        Page<Venue> paginaMock = new PageImpl<>(contenido, pageable, 1);
        when(venueRepository.findAll(pageable)).thenReturn(paginaMock);

        // Act
        Page<Venue> result = venueService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(venueRepository, times(1)).findAll(pageable);
    }


    @Test
    void findById_ExistingId_ReturnsVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", 300);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        Venue result = venueService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(venueRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> venueService.findById(99L));
    }

    @Test
    void update_ValidData_ReturnsUpdatedVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Nombre Viejo", "Dirección Vieja", 100);
        Venue datosNuevos = new Venue(null, "Nombre Nuevo", "Dirección Nueva", 200);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueRepository.save(any(Venue.class))).thenReturn(existente);

        // Act
        Venue result = venueService.update(1L, datosNuevos);

        // Assert
        assertNotNull(result);
        assertEquals("Nombre Nuevo", result.getNombre());
        assertEquals(200, result.getCapacidad());
        verify(venueRepository, times(1)).save(existente);
    }

    @Test
    void update_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Venue datosNuevos = new Venue(null, "Nombre Nuevo", "Dirección Nueva", 200);
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> venueService.update(99L, datosNuevos));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void update_InvalidData_ThrowsInvalidDataException() {
        // Arrange
        Venue existente = new Venue(1L, "Nombre Viejo", "Dirección Vieja", 100);
        Venue datosInvalidos = new Venue(null, "Nombre Nuevo", "Dirección Nueva", -10);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> venueService.update(1L, datosInvalidos));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void delete_ExistingId_DeletesVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", 300);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        venueService.delete(1L);

        // Assert
        verify(venueRepository, times(1)).delete(existente);
    }

    @Test
    void delete_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> venueService.delete(99L));
        verify(venueRepository, never()).delete(any());
    }
}
