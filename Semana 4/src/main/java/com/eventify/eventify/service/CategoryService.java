package com.eventify.eventify.service;

import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll(){
        return categoryRepository.findAll(Sort.by("nombre"));
    }

    public List<Category> findAllByIds(Set<Long> ids){
        if(ids.isEmpty()){
            return List.of();
        }
        List<Category> categorias = categoryRepository.findAllById(ids);
        if(categorias.size() != ids.size()){
            throw new ResourceNotFoundException("Una o más categorías no existen: " + ids);
        }
        return categorias;
    }
}
