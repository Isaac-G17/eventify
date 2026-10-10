package com.eventify.eventify.mapper;

import com.eventify.eventify.dto.VenueCreateDTO;
import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.model.Venue;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VenueMapper {

    VenueResponseDTO toResponse(Venue venue);

    @Mapping(target = "id", ignore = true)
    Venue toEntity(VenueCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(VenueCreateDTO dto, @MappingTarget Venue venue);

    VenueCreateDTO toForm(Venue venue);
}
