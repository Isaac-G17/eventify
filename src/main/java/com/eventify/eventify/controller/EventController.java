package com.eventify.eventify.controller;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.service.EventService;
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
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Operaciones para registrar y consultar eventos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento",description = "Valida y almacena un evento nuevo")
    public Event create(@Valid @RequestBody Event event){
        return eventService.save(event);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar eventos paginados", description = "Retorna un listado paginado de eventos. Soporta page, size y sort (ej: ?page=0&size=10&sort=nombre,asc)")
    public Page<Event> getAll(@ParameterObject Pageable pageable){
        return eventService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar evento por ID",description = "Retorna el evento solicitado, o 404 si no existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un evento con ese ID")
    })

    public  Event getById(@PathVariable Long id){
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar un evento existente", description = "Modifica los datos de un evento; retorna 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe un evento con ese ID")
    })
    public Event update(@PathVariable Long id, @Valid @RequestBody Event event){
            return eventService.update(id,event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento", description = "Elimina el evento indicado; retorna 404 si no existe, 204 si se elimina correctamente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un evento con ese ID")
    })
    public void delete(@PathVariable Long id){
        eventService.delete(id);
    }
}
