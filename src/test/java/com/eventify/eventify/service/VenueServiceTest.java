package com.eventify.eventify.service;


import com.eventify.eventify.dto.VenueCreateDTO;
import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.DuplicateResourceException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.mapper.VenueMapperImpl;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

/**
 * Lógica del servicio con el mapper real de MapStruct (VenueMapperImpl) y el repositorio simulado.
 * Las validaciones de formato (nombre vacío, capacidad mínima...) están en VenueCreateDTOValidationTest.
 */
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    private VenueService venueService;

    private VenueCreateDTO validVenue;

    @BeforeEach
    void setUp() {
        venueService = new VenueService(venueRepository, new VenueMapperImpl());
        validVenue = venueDto(null, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
    }

    @Test
    void save_ValidVenue_ReturnsResponseDto() {
        // Arrange (Preparar)
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> {
            Venue v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        // Act (Actuar)
        VenueResponseDTO result = venueService.save(validVenue);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Auditorio Principal", result.nombre());
        assertEquals("Bogotá", result.ciudad());
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void save_DuplicateName_ThrowsDuplicateResourceException() {
        // Arrange
        when(venueRepository.existsByNombreIgnoreCase("Auditorio Principal")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> venueService.save(validVenue));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void findAll_WithPageable_ReturnsPageOfResponseDtos() {
        // Arrange
        List<Venue> contenido = List.of(new Venue(1L, "Lugar A", "Dirección A", "Bogotá", 100));
        Pageable pageable = PageRequest.of(0, 5);
        when(venueRepository.findAll(pageable)).thenReturn(new PageImpl<>(contenido, pageable, 1));

        // Act
        Page<VenueResponseDTO> result = venueService.findAll(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Lugar A", result.getContent().get(0).nombre());
        verify(venueRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_ExistingId_ReturnsVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        VenueResponseDTO result = venueService.findById(1L);

        // Assert
        assertEquals(1L, result.id());
        assertEquals(300, result.capacidad());
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> venueService.findById(99L));
        assertEquals("El lugar con ID 99 no existe", ex.getMessage());
    }

    @Test
    void findFormById_ReturnsDtoWithIdForEditForm() {
        // Arrange
        when(venueRepository.findById(1L)).thenReturn(Optional.of(new Venue(1L, "Auditorio Principal", "Calle 50", "Bogotá", 300)));

        // Act
        VenueCreateDTO form = venueService.findFormById(1L);

        // Assert
        assertEquals(1L, form.getId());
        assertEquals("Auditorio Principal", form.getNombre());
    }

    @Test
    void update_ValidData_ReturnsUpdatedVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Nombre Viejo", "Dirección Vieja", "Bogotá", 100);
        VenueCreateDTO datosNuevos = venueDto(1L, "Nombre Nuevo", "Dirección Nueva", "Cali", 200);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueRepository.save(existente)).thenReturn(existente);

        // Act
        VenueResponseDTO result = venueService.update(1L, datosNuevos);

        // Assert
        assertEquals("Nombre Nuevo", result.nombre());
        assertEquals("Cali", result.ciudad());
        assertEquals(200, result.capacidad());
        assertEquals(1L, existente.getId(), "El id de la entidad nunca cambia");
        verify(venueRepository, times(1)).save(existente);
    }

    @Test
    void update_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        VenueCreateDTO datosNuevos = venueDto(99L, "Nombre Nuevo", "Dirección Nueva", "Bogotá", 200);
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> venueService.update(99L, datosNuevos));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void update_IdMismatch_ThrowsBusinessRuleViolationException() {
        // Arrange
        VenueCreateDTO datosNuevos = venueDto(2L, "Nombre Nuevo", "Dirección Nueva", "Bogotá", 200);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> venueService.update(1L, datosNuevos));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void update_NameUsedByAnotherVenue_ThrowsDuplicateResourceException() {
        // Arrange
        Venue existente = new Venue(1L, "Nombre Viejo", "Dirección Vieja", "Bogotá", 100);
        VenueCreateDTO datosNuevos = venueDto(1L, "Teatro Municipal", "Dirección Nueva", "Bogotá", 200);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueRepository.existsByNombreIgnoreCaseAndIdNot("Teatro Municipal", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> venueService.update(1L, datosNuevos));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void delete_ExistingId_DeletesVenue() {
        // Arrange
        Venue existente = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
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

    @Test
    void delete_VenueWithEvents_ThrowsBusinessRuleViolationException() {
        // Arrange
        Venue existente = new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(venueRepository.contarEventosAsociados(1L)).thenReturn(3L);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> venueService.delete(1L));
        verify(venueRepository, never()).delete(any());
    }

    @Test
    void save_MapsDtoToEntityWithoutId() {
        // Arrange
        validVenue.setId(50L);
        ArgumentCaptor<Venue> guardado = ArgumentCaptor.forClass(Venue.class);
        when(venueRepository.save(guardado.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        venueService.save(validVenue);

        // Assert: el id lo genera la BD, nunca el cliente
        assertNull(guardado.getValue().getId());
    }

    static VenueCreateDTO venueDto(Long id, String nombre, String direccion, String ciudad, Integer capacidad) {
        VenueCreateDTO dto = new VenueCreateDTO();
        dto.setId(id);
        dto.setNombre(nombre);
        dto.setDireccion(direccion);
        dto.setCiudad(ciudad);
        dto.setCapacidad(capacidad);
        return dto;
    }
}
