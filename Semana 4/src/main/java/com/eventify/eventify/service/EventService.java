package com.eventify.eventify.service;

import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class EventService {
    private final EventRepository eventRepository;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public EventService(EventRepository eventRepository, VenueService venueService, CategoryService categoryService){
        this.eventRepository = eventRepository;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    @Transactional
    public Event save(Event event){
        validate(event);
        asignarRelaciones(event);
        event.setActivo(true);
        // La cascada PERSIST/MERGE de la relación inserta también los vínculos en events_categories
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Event findById(Long id){
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró el evento con id " + id));
    }

    @Transactional
    public Event update(Long id,Event datosActualizados){
        Event eventoExistente = findById(id);
        validate(datosActualizados);
        asignarRelaciones(datosActualizados);

        eventoExistente.setNombre(datosActualizados.getNombre());
        eventoExistente.setFecha(datosActualizados.getFecha());
        eventoExistente.setDescripcion(datosActualizados.getDescripcion());
        eventoExistente.setVenue(datosActualizados.getVenue());
        eventoExistente.getCategorias().clear();
        eventoExistente.getCategorias().addAll(datosActualizados.getCategorias());

        return eventRepository.save(eventoExistente);
    }

    /** Borrado lógico: el evento queda inactivo y deja de aparecer en las consultas. */
    @Transactional
    public void delete(Long id){
        findById(id);
        eventRepository.softDelete(id);
    }

    @Transactional(readOnly = true)
    public Slice<EventSummaryDTO> buscar(EventFilter filtro, Pageable pageable){
        if(filtro.fechaDesde() != null && filtro.fechaHasta() != null && filtro.fechaDesde().isAfter(filtro.fechaHasta())){
            throw new InvalidDataException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return eventRepository.buscarResumen(filtro.ciudad(), filtro.categoria(), filtro.capacidadMinima(),
                filtro.fechaDesde(), filtro.fechaHasta(), pageable);
    }

    /** Reemplaza las referencias que llegan solo con id por las entidades reales de la BD. */
    private void asignarRelaciones(Event event){
        event.setVenue(venueService.findById(event.getVenue().getId()));

        Set<Long> categoriaIds = event.getCategorias() == null ? Set.of() : event.getCategorias().stream()
                .map(Category::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        event.setCategorias(new HashSet<>(categoryService.findAllByIds(categoriaIds)));
    }

    private void validate(Event event){
        if(event.getNombre() == null || event.getNombre().trim().isEmpty()){
            throw new InvalidDataException("El nombre del evento no puede estar vacío");
        }
        if(event.getFecha() == null){
            throw new InvalidDataException("La fecha del evento es obligatoria");
        }
        if(event.getFecha().isBefore(LocalDate.now())){
            throw new InvalidDataException("La fecha del evento no puede estar en el pasado");
        }
        if(event.getDescripcion() == null || event.getDescripcion().trim().isEmpty()){
            throw new InvalidDataException("La descripción del evento no puede estar vacía");
        }
        if(event.getDescripcion().length() > 500){
            throw new InvalidDataException("La descripción no puede superar 500 caracteres");
        }
        if(event.getVenue() == null || event.getVenue().getId() == null){
            throw new InvalidDataException("El lugar del evento es obligatorio");
        }
    }
}
