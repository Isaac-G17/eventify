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
        if(venue.getDireccion() == null || venue.getDireccion().trim().isEmpty()){
            throw new InvalidDataException("La dirección no puede estar vacía");
        }
        if(venue.getCapacidad() == null){
            throw new InvalidDataException("La capacidad es obligatoria");
        }
        if(venue.getCapacidad() <= 0){
            throw new InvalidDataException("La capacidad debe ser mayor que cero");
        }
        return venueRepository.save(venue);
    }

    public List<Venue> findAll(){
        return venueRepository.findAll();
    }
}
