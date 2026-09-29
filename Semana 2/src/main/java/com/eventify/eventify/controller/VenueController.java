package com.eventify.eventify.controller;

import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/venues")
@Tag(name = "Lugares", description = "Operaciones para registrar y consultar lugares (venues)")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo lugar", description = "Valida y almacena un lugar en memoria")
    public Venue create(@Valid @RequestBody Venue venue) {
        return venueService.save(venue);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar un lugar por ID", description = "Retorna el lugar solicitado, o 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID")
    })
    public Venue getById(@PathVariable Long id){
        return venueService.findById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar lugares paginados", description = "Retorna un listado paginado de lugares. Soporta page, size y sort (ej: ?page=0&size=10&sort=nombre,asc)")
    public Page<Venue> getAll(@ParameterObject Pageable pageable) {
        return venueService.findAll(pageable);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar un lugar existente", description = "Modifica los datos de un lugar; retorna 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID")
    })
    public Venue update(@PathVariable Long id, @Valid @RequestBody Venue venue){
        return venueService.update(id, venue);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un lugar", description = "Elimina el lugar indicado; retorna 404 si no existe, 204 si se elimina correctamente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID")
    })
    public void delete(@PathVariable Long id){
        venueService.delete(id);
    }
}
