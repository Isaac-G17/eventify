package com.eventify.eventify.mapper;

import com.eventify.eventify.dto.CategoryResponseDTO;
import com.eventify.eventify.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryResponseDTO toResponse(Category category);

    List<CategoryResponseDTO> toResponse(List<Category> categories);
}
