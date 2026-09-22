package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    public Event save(Event event){
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
        return eventRepository.save(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }
}
