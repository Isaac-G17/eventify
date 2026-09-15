package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository){
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue){
        if(venue.getNombre() == null || venue.getNombre().trim().isEmpty()){
            throw new InvalidDataException("El nombre del lugar no puede estar vacío");
        }
        return venueRepository.save(venue);
    }

    public List<Venue> findAll(){
        return venueRepository.findAll();
    }
}
