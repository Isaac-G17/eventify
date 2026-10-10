package com.eventify.eventify.controller;

import com.eventify.eventify.dto.VenueCreateDTO;
import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.service.VenueService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.eventify.eventify.docs.ProblemDetailExamples.*;


@RestController
@RequestMapping("/api/venues")
@Tag(name = "Lugares", description = "Registro y consulta de lugares (venues) donde se realizan los eventos")
@ApiResponse(responseCode = "500", description = "Error inesperado (sin detalles internos)",
        content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = ERROR_500)))
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo lugar", description = "Recibe un VenueCreateDTO (sin id), valida sus datos y lo almacena. El nombre del lugar no se puede repetir")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: el campo 'errores' indica el problema de cada campo",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = VALIDACION_400))),
            @ApiResponse(responseCode = "409", description = "Ya existe un lugar con ese nombre",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = DUPLICADO_409)))
    })
    public VenueResponseDTO create(@Validated(OnCreate.class) @RequestBody VenueCreateDTO venue) {
        return venueService.save(venue);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consultar un lugar por ID", description = "Retorna el VenueResponseDTO del lugar solicitado, o 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = LUGAR_404)))
    })
    public VenueResponseDTO getById(@Parameter(description = "ID del lugar", example = "1") @PathVariable Long id){
        return venueService.findById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar lugares paginados", description = "Retorna una página de VenueResponseDTO. Soporta page, size y sort (ej: ?page=0&size=10&sort=nombre,asc)")
    public Page<VenueResponseDTO> getAll(@ParameterObject Pageable pageable) {
        return venueService.findAll(pageable);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar un lugar existente", description = "Recibe un VenueCreateDTO con el id del lugar (obligatorio y debe coincidir con el de la URL); retorna 404 si no existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye el id faltante en el cuerpo)",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = VALIDACION_400))),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = LUGAR_404))),
            @ApiResponse(responseCode = "409", description = "Ya existe otro lugar con ese nombre",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = DUPLICADO_409))),
            @ApiResponse(responseCode = "422", description = "El id del cuerpo no coincide con el de la URL",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = REGLA_NEGOCIO_422)))
    })
    public VenueResponseDTO update(@Parameter(description = "ID del lugar", example = "1") @PathVariable Long id,
                                   @Validated(OnUpdate.class) @RequestBody VenueCreateDTO venue){
        return venueService.update(id, venue);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un lugar", description = "Elimina el lugar indicado. No se permite si tiene eventos asociados (incluidos los eliminados lógicamente, que se conservan por trazabilidad)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un lugar con ese ID",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = LUGAR_404))),
            @ApiResponse(responseCode = "422", description = "El lugar tiene eventos asociados",
                    content = @Content(mediaType = MEDIA_TYPE, schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = REGLA_NEGOCIO_422)))
    })
    public void delete(@Parameter(description = "ID del lugar", example = "1") @PathVariable Long id){
        venueService.delete(id);
    }
}
