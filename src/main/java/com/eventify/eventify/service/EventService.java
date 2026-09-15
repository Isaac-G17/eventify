package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.repository.EventRepository;
import org.springframework.stereotype.Service;

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
        return eventRepository.save(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }
}
