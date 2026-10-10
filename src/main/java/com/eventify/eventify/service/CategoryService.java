package com.eventify.eventify.service;

import com.eventify.eventify.dto.CategoryResponseDTO;
import com.eventify.eventify.mapper.CategoryMapper;
import com.eventify.eventify.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll(){
        return categoryMapper.toResponse(categoryRepository.findAll(Sort.by("nombre")));
    }
}
