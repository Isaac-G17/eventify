package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventResponseDTO;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.validation.OnCreate;
import com.eventify.eventify.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.eventify.eventify.docs.ProblemDetailExamples.*;


@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Catálogo, búsqueda y gestión de eventos. Los eventos eliminados (borrado lógico) nunca aparecen en las consultas")
// Aplica a todas las operaciones: la red de seguridad del manejador global
@ApiResponse(responseCode = "500", description = "Error inesperado (sin detalles internos)",
        content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = ERROR_500)))
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo evento",
            description = "Recibe un EventCreateDTO (sin id), valida sus datos y lo almacena. venueId y categoryIds se resuelven " +
                    "a entidades con MapStruct y los vínculos con las categorías se guardan en cascada. Retorna el EventResponseDTO con los nombres resueltos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: el campo 'errores' indica el problema de cada campo",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = VALIDACION_400))),
            @ApiResponse(responseCode = "404", description = "El lugar o alguna de las categorías no existen",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = RELACION_404)))
    })
    public EventResponseDTO create(@Validated(OnCreate.class) @RequestBody EventCreateDTO event){
        return eventService.save(event);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Catálogo de eventos (resumen)",
            description = "Retorna un Slice del Record EventSummaryDTO (nombre, fecha, lugar y ciudad) ordenado por fecha descendente. " +
                    "Es la proyección optimizada de la semana 4: se arma en una sola consulta JPQL y Slice no calcula el total de registros " +
                    "(usa 'last' para saber si hay más). Solo incluye eventos activos. Soporta page y size (ej: ?page=0&size=10)")
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
            @ApiResponse(responseCode = "422", description = "Rango de fechas inválido (fechaDesde posterior a fechaHasta)",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = REGLA_NEGOCIO_422)))
    })
    public Slice<EventSummaryDTO> search(@ParameterObject EventFilter filtro, @ParameterObject Pageable pageable){
        return eventService.buscar(filtro, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar evento por ID",
            description = "Retorna un EventResponseDTO (mapeado con MapStruct): el lugar y las categorías llegan como texto (venueName, categoryNames), " +
                    "sin IDs internos. Retorna 404 si no existe o si fue eliminado lógicamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = EVENTO_404)))
    })

    public  EventResponseDTO getById(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id){
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar un evento existente",
            description = "Recibe un EventCreateDTO con el id del evento (obligatorio y debe coincidir con el de la URL) y reemplaza sus datos, " +
                    "lugar y categorías. Retorna 404 si no existe o fue eliminado lógicamente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye el id faltante en el cuerpo)",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = VALIDACION_400))),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID, o el lugar/categoría no existen",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = EVENTO_404))),
            @ApiResponse(responseCode = "422", description = "El id del cuerpo no coincide con el de la URL",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = REGLA_NEGOCIO_422)))
    })
    public EventResponseDTO update(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id,
                                   @Validated(OnUpdate.class) @RequestBody EventCreateDTO event){
            return eventService.update(id,event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un evento (borrado lógico)",
            description = "No borra el registro físicamente: lo marca como inactivo (activo = false) para conservar la trazabilidad histórica. " +
                    "Desde ese momento el evento deja de aparecer en el catálogo, la búsqueda y la consulta por ID. Retorna 404 si no existe o ya estaba eliminado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un evento activo con ese ID",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = EVENTO_404)))
    })
    public void delete(@Parameter(description = "ID del evento", example = "1") @PathVariable Long id){
        eventService.delete(id);
    }
}
