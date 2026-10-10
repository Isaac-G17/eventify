package com.eventify.eventify.service;

import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventResponseDTO;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.mapper.EventMapper;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Recibe DTOs de entrada y retorna DTOs de salida: la entidad Event no sale de la capa de persistencia.
 * - CRUD individual: EventCreateDTO -> MapStruct -> Event -> MapStruct -> EventResponseDTO
 * - Listados masivos: Record EventSummaryDTO construido directamente en JPQL (Slice), sin pasar por el mapper
 */
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper){
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public EventResponseDTO save(EventCreateDTO dto){
        // MapStruct resuelve venueId y categoryIds a entidades; la cascada guarda los vínculos en events_categories
        Event event = eventMapper.toEntity(dto);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponseDTO findById(Long id){
        return eventMapper.toResponse(buscarEntidad(id));
    }

    /** Datos actuales del evento en el formato del formulario de edición. */
    @Transactional(readOnly = true)
    public EventCreateDTO findFormById(Long id){
        return eventMapper.toForm(buscarEntidad(id));
    }

    @Transactional
    public EventResponseDTO update(Long id, EventCreateDTO dto){
        if(dto.getId() != null && !dto.getId().equals(id)){
            throw new BusinessRuleViolationException("El ID del cuerpo (" + dto.getId() + ") no coincide con el de la URL (" + id + ")");
        }
        Event eventoExistente = buscarEntidad(id);
        eventMapper.updateEntity(dto, eventoExistente);
        return eventMapper.toResponse(eventRepository.save(eventoExistente));
    }

    /** Borrado lógico: el evento queda inactivo y deja de aparecer en las consultas. */
    @Transactional
    public void delete(Long id){
        buscarEntidad(id);
        eventRepository.softDelete(id);
    }

    @Transactional(readOnly = true)
    public Slice<EventSummaryDTO> buscar(EventFilter filtro, Pageable pageable){
        if(filtro.fechaDesde() != null && filtro.fechaHasta() != null && filtro.fechaDesde().isAfter(filtro.fechaHasta())){
            throw new BusinessRuleViolationException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return eventRepository.buscarResumen(filtro.ciudad(), filtro.categoria(), filtro.capacidadMinima(),
                filtro.fechaDesde(), filtro.fechaHasta(), pageable);
    }

    private Event buscarEntidad(Long id){
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("El evento con ID " + id + " no existe"));
    }
}
