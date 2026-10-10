package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Operaciones para registrar y consultar eventos. Los eventos eliminados (borrado lógico) nunca aparecen en las consultas")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento",
            description = "Valida y almacena un evento nuevo. El lugar es obligatorio y se indica por id (\"venue\": {\"id\": 1}); " +
                    "las categorías son opcionales (\"categorias\": [{\"id\": 1}]). Los vínculos con las categorías se guardan en cascada. " +
                    "El evento siempre se crea activo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o lugar no indicado"),
            @ApiResponse(responseCode = "404", description = "El lugar o alguna categoría no existen")
    })
    public Event create(@Valid @RequestBody Event event){
        return eventService.save(event);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Catálogo de eventos (resumen)",
            description = "Retorna un Slice de EventSummaryDTO (nombre, fecha, lugar y ciudad) ordenado por fecha descendente. " +
                    "Slice no calcula el total de registros, por eso es más rápido que una página tradicional: usa 'hasNext' para saber si hay más. " +
                    "Solo incluye eventos activos: los eliminados lógicamente se excluyen automáticamente. Soporta page y size (ej: ?page=0&size=10)")
    public Slice<EventSummaryDTO> getAll(@ParameterObject Pageable pageable){
        return eventService.buscar(EventFilter.sinFiltros(), pageable);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar eventos con filtros relacionales",
            description = "Filtra el catálogo por datos del lugar (ciudad, capacidad), de las categorías (nombre) y por rango de fechas. " +
                    "Todos los filtros son opcionales y se combinan con AND. Ciudad y categoría aceptan texto parcial sin importar mayúsculas " +
                    "(ej: ciudad=bog y categoria=rock). El resultado es un Slice de EventSummaryDTO ordenado por fecha descendente " +
                    "y nunca incluye eventos eliminados lógicamente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de la búsqueda (puede ser vacío)"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido (fechaDesde posterior a fechaHasta)")
    })
    public Slice<EventSummaryDTO> search(@ParameterObject EventFilter filtro, @ParameterObject Pageable pageable){
        return eventService.buscar(filtro, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar evento por ID",
            description = "Retorna el evento con su lugar y categorías (cargados en una sola consulta). " +
                    "Retorna 404 si no existe o si fue eliminado lógicamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID")
    })

    public  Event getById(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id){
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar un evento existente",
            description = "Modifica los datos, el lugar y las categorías de un evento activo; retorna 404 si no existe o fue eliminado lógicamente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID, o el lugar/categoría no existen")
    })
    public Event update(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id, @Valid @RequestBody Event event){
            return eventService.update(id,event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento (borrado lógico)",
            description = "No borra el registro físicamente: lo marca como inactivo (activo = false) para conservar la trazabilidad histórica. " +
                    "Desde ese momento el evento deja de aparecer en el catálogo, la búsqueda y la consulta por ID. Retorna 404 si no existe o ya estaba eliminado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID")
    })
    public void delete(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id){
        eventService.delete(id);
    }
}
