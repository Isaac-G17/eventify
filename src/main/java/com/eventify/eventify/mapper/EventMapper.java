package com.eventify.eventify.mapper;

import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventResponseDTO;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import org.mapstruct.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapeo Event <-> DTOs. MapStruct genera la implementación (EventMapperImpl) al compilar.
 * Los listados masivos NO pasan por aquí: siguen usando el Record EventSummaryDTO construido en JPQL.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ReferenceMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventMapper {

    /** Entidad -> respuesta: las relaciones se aplanan a texto (sin IDs internos). */
    @Mapping(target = "venueName", source = "venue.nombre")
    @Mapping(target = "venueCity", source = "venue.ciudad")
    @Mapping(target = "categoryNames", source = "categorias")
    EventResponseDTO toResponse(Event event);

    /** Entrada -> entidad nueva: venueId y categoryIds se resuelven contra la BD (ReferenceMapper). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "venue", source = "venueId")
    @Mapping(target = "categorias", source = "categoryIds")
    Event toEntity(EventCreateDTO dto);

    /** Entrada -> entidad existente (actualización). El id y el estado activo nunca se modifican desde el cliente. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "venue", source = "venueId")
    @Mapping(target = "categorias", source = "categoryIds")
    void updateEntity(EventCreateDTO dto, @MappingTarget Event event);

    /** Entidad -> formulario de edición del panel (precarga el lugar y las categorías marcadas). */
    @Mapping(target = "venueId", source = "venue.id")
    @Mapping(target = "categoryIds", source = "categorias")
    EventCreateDTO toForm(Event event);

    default List<String> nombresDeCategorias(Set<Category> categorias) {
        return categorias.stream().map(Category::getNombre).sorted().toList();
    }

    default Set<Long> idsDeCategorias(Set<Category> categorias) {
        return categorias.stream().map(Category::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
