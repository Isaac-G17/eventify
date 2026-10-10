package com.eventify.eventify.mapper;

import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.repository.CategoryRepository;
import com.eventify.eventify.repository.VenueRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convierte los IDs que llegan en los DTOs de entrada en entidades reales de la BD.
 * MapStruct lo usa automáticamente (EventMapper declara "uses = ReferenceMapper.class")
 * cada vez que debe pasar de Long a Venue o de Set<Long> a Set<Category>.
 */
@Component
public class ReferenceMapper {

    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;

    public ReferenceMapper(VenueRepository venueRepository, CategoryRepository categoryRepository) {
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
    }

    public Venue toVenue(Long venueId) {
        if (venueId == null) {
            return null;
        }
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("El lugar con ID " + venueId + " no existe"));
    }

    public Set<Category> toCategorias(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Category> encontradas = categoryRepository.findAllById(categoryIds);
        if (encontradas.size() != categoryIds.size()) {
            Set<Long> faltantes = new TreeSet<>(categoryIds);
            encontradas.forEach(categoria -> faltantes.remove(categoria.getId()));
            throw new ResourceNotFoundException("Las categorías con ID " + faltantes + " no existen");
        }
        return new HashSet<>(encontradas);
    }
}
