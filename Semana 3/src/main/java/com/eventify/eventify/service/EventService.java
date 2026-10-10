package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDate;


@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    public Event save(Event event){
        validate(event);
        return eventRepository.save(event);
    }

    public Event findById(Long id){
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró el evento con id " + id));
    }

    public Event update(Long id,Event datosActualizados){
        Event eventoExistente = findById(id);
        validate(datosActualizados);

        eventoExistente.setNombre(datosActualizados.getNombre());
        eventoExistente.setFecha(datosActualizados.getFecha());
        eventoExistente.setDescripcion(datosActualizados.getDescripcion());

        return eventRepository.save(eventoExistente);
    }

    public void delete(Long id){
        Event evento = findById(id);
        eventRepository.delete(evento);
    }

    public Page<Event> findAll(Pageable pageable){
        return eventRepository.findAll(pageable);
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
    }
}
