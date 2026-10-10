package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventResponseDTO;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void create_NombreVacioYFechaPasada_Retorna400ConMapaDeErrores() throws Exception {
        // Escenario 2
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "fecha": "2020-01-01", "descripcion": "Prueba", "venueId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Datos de entrada inválidos"))
                .andExpect(jsonPath("$.instance").value("/api/events"))
                .andExpect(jsonPath("$.errores.nombre").value("no debe estar vacío"))
                // Escenario 4: el mapa prioriza el mensaje de negocio de @NoPastEvents...
                .andExpect(jsonPath("$.errores.fecha").value("Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó"))
                // ...y "detalles" conserva también el mensaje genérico de @Future
                .andExpect(jsonPath("$.detalles[?(@.codigo == 'Future')].mensaje").value("debe ser una fecha futura"));

        verify(eventService, never()).save(any());
    }

    @Test
    void create_ConId_Retorna400PorGrupoOnCreate() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 7, "nombre": "Evento válido", "fecha": "%s", "descripcion": "Prueba", "venueId": 1}
                                """.formatted(LocalDate.now().plusMonths(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.id").value("no debe enviarse al crear un evento"));
    }

    @Test
    void create_FechaMalFormada_Retorna400DeFormato() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Evento válido", "fecha": "20-12-2026", "descripcion": "Prueba", "venueId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Formato de petición inválido"));
    }

    @Test
    void create_Valido_Retorna201ConResponseDto() throws Exception {
        EventResponseDTO respuesta = new EventResponseDTO(201L, "Evento válido", LocalDate.now().plusMonths(1), "Prueba", null,
                "Centro de Convenciones Principal", "Bogotá", List.of("Conciertos", "Conferencias", "Gastronomía"));
        when(eventService.save(any())).thenReturn(respuesta);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Evento válido", "fecha": "%s", "descripcion": "Prueba", "venueId": 1, "categoryIds": [1, 3, 5]}
                                """.formatted(LocalDate.now().plusMonths(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venueName").value("Centro de Convenciones Principal"))
                .andExpect(jsonPath("$.categoryNames[2]").value("Gastronomía"));
    }

    @Test
    void getById_Existente_RetornaDtoPlanoSinIdsInternos() throws Exception {
        // Escenario 1
        when(eventService.findById(21L)).thenReturn(new EventResponseDTO(21L, "Concierto de ROCK", LocalDate.of(2026, 12, 20),
                "Noche de rock", null, "Estadio Cubierto Los Andes", "Bogotá", List.of("Conciertos", "Rock")));

        mockMvc.perform(get("/api/events/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venueName").value("Estadio Cubierto Los Andes"))
                .andExpect(jsonPath("$.categoryNames[1]").value("Rock"))
                .andExpect(jsonPath("$.venue").doesNotExist())
                .andExpect(jsonPath("$.venueId").doesNotExist())
                .andExpect(jsonPath("$.categorias").doesNotExist())
                .andExpect(jsonPath("$.activo").doesNotExist());
    }

    @Test
    void getById_Inexistente_Retorna404ConProblemDetail() throws Exception {
        // Escenario 5
        when(eventService.findById(9999L)).thenThrow(new ResourceNotFoundException("El evento con ID 9999 no existe"));

        mockMvc.perform(get("/api/events/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.eventify.com/errores/recurso-no-encontrado"))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("El evento con ID 9999 no existe"))
                .andExpect(jsonPath("$.instance").value("/api/events/9999"));
    }

    @Test
    void getById_IdNoNumerico_Retorna400() throws Exception {
        mockMvc.perform(get("/api/events/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Formato de petición inválido"));
    }

    @Test
    void update_SinIdEnCuerpo_Retorna400PorGrupoOnUpdate() throws Exception {
        mockMvc.perform(put("/api/events/21")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Evento válido", "fecha": "%s", "descripcion": "Prueba", "venueId": 1}
                                """.formatted(LocalDate.now().plusMonths(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.id").value("es obligatorio al actualizar un evento"));
    }

    @Test
    void getAll_SigueUsandoElRecordEventSummaryDTO() throws Exception {
        // Escenario 7: el listado masivo conserva el Record con Slice
        when(eventService.buscar(any(), eq(PageRequest.of(0, 10)))).thenReturn(new SliceImpl<>(
                List.of(new EventSummaryDTO(21L, "Concierto de ROCK", LocalDate.of(2026, 12, 20), "Estadio Cubierto Los Andes", "Bogotá")),
                PageRequest.of(0, 10), true));

        mockMvc.perform(get("/api/events").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lugar").value("Estadio Cubierto Los Andes"))
                .andExpect(jsonPath("$.content[0].ciudad").value("Bogotá"))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.totalElements").doesNotExist());
    }

    @Test
    void errorInesperado_Retorna500SinExponerDetallesInternos() throws Exception {
        when(eventService.findById(1L)).thenThrow(new IllegalStateException("conexión a BD caída: password=secreto"));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno del servidor"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secreto"))));
    }
}
