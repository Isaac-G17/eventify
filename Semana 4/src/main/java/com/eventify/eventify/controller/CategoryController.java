package com.eventify.eventify.controller;

import com.eventify.eventify.model.Category;
import com.eventify.eventify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías", description = "Clasificación temática de los eventos")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar categorías", description = "Retorna todas las categorías ordenadas por nombre. Sus ids se usan al registrar eventos y su nombre en el filtro 'categoria' de la búsqueda")
    public List<Category> getAll() {
        return categoryService.findAll();
    }
}
