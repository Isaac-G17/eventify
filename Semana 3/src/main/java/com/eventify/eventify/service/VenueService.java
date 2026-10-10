package com.eventify.eventify.service;

import com.eventify.eventify.exception.InvalidDataException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository){
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue){
        validate(venue);
        return venueRepository.save(venue);
    }

    public Venue findById(Long id){
        return venueRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("No se encontró el lugar con id" + id));
    }

    public Venue update(Long id, Venue datosActualizados){
        Venue venueExistente = findById(id);
        validate(datosActualizados);

        venueExistente.setNombre(datosActualizados.getNombre());
        venueExistente.setDireccion(datosActualizados.getDireccion());
        venueExistente.setCapacidad(datosActualizados.getCapacidad());

        return venueRepository.save(venueExistente);
    }

    public void delete(Long id){
        Venue venue = findById(id);
        venueRepository.delete(venue);
    }

    public Page<Venue> findAll(Pageable pageable){
        return venueRepository.findAll(pageable);
    }

    private void validate(Venue venue){
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
    }
}
