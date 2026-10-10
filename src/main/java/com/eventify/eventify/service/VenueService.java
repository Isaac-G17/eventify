package com.eventify.eventify.service;

import com.eventify.eventify.dto.VenueCreateDTO;
import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.DuplicateResourceException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.mapper.VenueMapper;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Recibe DTOs de entrada y retorna DTOs de salida: la entidad Venue no sale de la capa de persistencia.
 * El formato de los datos ya llega validado por Bean Validation (@Valid en el controlador);
 * aquí solo quedan las reglas que dependen de la BD (nombre único, eventos asociados).
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper){
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    @Transactional
    public VenueResponseDTO save(VenueCreateDTO dto){
        if(venueRepository.existsByNombreIgnoreCase(dto.getNombre().trim())){
            throw new DuplicateResourceException("Ya existe un lugar con el nombre '" + dto.getNombre().trim() + "'");
        }
        Venue venue = venueMapper.toEntity(dto);
        return venueMapper.toResponse(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public VenueResponseDTO findById(Long id){
        return venueMapper.toResponse(buscarEntidad(id));
    }

    /** Datos actuales del lugar en el formato del formulario de edición. */
    @Transactional(readOnly = true)
    public VenueCreateDTO findFormById(Long id){
        return venueMapper.toForm(buscarEntidad(id));
    }

    @Transactional
    public VenueResponseDTO update(Long id, VenueCreateDTO dto){
        if(dto.getId() != null && !dto.getId().equals(id)){
            throw new BusinessRuleViolationException("El ID del cuerpo (" + dto.getId() + ") no coincide con el de la URL (" + id + ")");
        }
        Venue venueExistente = buscarEntidad(id);
        if(venueRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)){
            throw new DuplicateResourceException("Ya existe otro lugar con el nombre '" + dto.getNombre().trim() + "'");
        }
        venueMapper.updateEntity(dto, venueExistente);
        return venueMapper.toResponse(venueRepository.save(venueExistente));
    }

    @Transactional
    public void delete(Long id){
        Venue venue = buscarEntidad(id);
        if(venueRepository.contarEventosAsociados(id) > 0){
            throw new BusinessRuleViolationException("No se puede eliminar el lugar porque tiene eventos asociados");
        }
        venueRepository.delete(venue);
    }

    @Transactional(readOnly = true)
    public Page<VenueResponseDTO> findAll(Pageable pageable){
        return venueRepository.findAll(pageable).map(venueMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VenueResponseDTO> findAllOrdenados(){
        return venueRepository.findAll(Sort.by("nombre")).stream().map(venueMapper::toResponse).toList();
    }

    private Venue buscarEntidad(Long id){
        return venueRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("El lugar con ID " + id + " no existe"));
    }
}
