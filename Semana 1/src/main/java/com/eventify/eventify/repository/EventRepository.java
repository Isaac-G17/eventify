package com.eventify.eventify.repository;

import com.eventify.eventify.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepository {

    private final List<Event> events = new ArrayList<>();
    private Long idCounter = 1L;

    public Event save(Event event){
        event.setId(idCounter++);
        events.add(event);
        return event;
    }

    public List<Event> findAll(){
        return events;
    }
}
