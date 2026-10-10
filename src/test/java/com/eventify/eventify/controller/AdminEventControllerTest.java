package com.eventify.eventify.controller;

import com.eventify.eventify.dto.CategoryResponseDTO;
import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.service.CategoryService;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private CategoryService categoryService;

    private final String fechaFutura = LocalDate.now().plusMonths(1).toString();

    @BeforeEach
    void opcionesDelFormulario() {
        when(venueService.findAllOrdenados()).thenReturn(List.of(new VenueResponseDTO(5L, "Estadio Cubierto Los Andes", "Carrera 45", "Bogotá", 1200)));
        when(categoryService.findAll()).thenReturn(List.of(new CategoryResponseDTO(1L, "Conciertos", "Música en vivo"),
                new CategoryResponseDTO(8L, "Rock", "Género rock")));
    }

    @Test
    void listar_ConEventos_RetornaVistaConModelo() throws Exception {
        List<EventSummaryDTO> eventos = List.of(
                new EventSummaryDTO(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Auditorio Principal", "Bogotá")
        );
        Slice<EventSummaryDTO> slice = new SliceImpl<>(eventos);
        when(eventService.buscar(any(), any())).thenReturn(slice);

        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(model().attributeExists("eventos"))
                .andExpect(model().attribute("eventos", slice))
                .andExpect(content().string(containsString("Auditorio Principal")))
                .andExpect(content().string(containsString("Bogotá")));
    }

    @Test
    void listar_SinEventos_RetornaVistaConListaVacia() throws Exception {
        Slice<EventSummaryDTO> sliceVacio = new SliceImpl<>(List.of());
        when(eventService.buscar(any(), any())).thenReturn(sliceVacio);

        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(model().attribute("eventos", sliceVacio))
                .andExpect(content().string(containsString("Actualmente no hay eventos programados.")));
    }

    @Test
    void listar_ConFiltros_PasaLosFiltrosAlServicioYLosConservaEnLaPaginacion() throws Exception {
        Pageable pageable = PageRequest.of(0, 8);
        List<EventSummaryDTO> eventos = List.of(
                new EventSummaryDTO(21L, "Concierto de ROCK", LocalDate.of(2026, 12, 20), "Estadio Cubierto Los Andes", "Bogotá")
        );
        when(eventService.buscar(any(), any())).thenReturn(new SliceImpl<>(eventos, pageable, true));

        mockMvc.perform(get("/admin/eventos").param("ciudad", "bog").param("categoria", "rock"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("urlSiguiente", "/admin/eventos?ciudad=bog&categoria=rock&page=1"))
                .andExpect(content().string(containsString("/admin/eventos?ciudad=bog&amp;categoria=rock&amp;page=1")))
                .andExpect(content().string(containsString("value=\"rock\"")));

        ArgumentCaptor<EventFilter> filtro = ArgumentCaptor.forClass(EventFilter.class);
        verify(eventService).buscar(filtro.capture(), any());
        assertEquals("bog", filtro.getValue().ciudad());
        assertEquals("rock", filtro.getValue().categoria());
    }

    @Test
    void listar_PrimeraYUltimaPagina_DeshabilitaLosBotones() throws Exception {
        Pageable segunda = PageRequest.of(1, 8);
        List<EventSummaryDTO> eventos = List.of(
                new EventSummaryDTO(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Auditorio Principal", "Bogotá")
        );
        when(eventService.buscar(any(), any())).thenReturn(new SliceImpl<>(eventos, segunda, false));

        mockMvc.perform(get("/admin/eventos").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("page=0")))
                .andExpect(content().string(not(containsString("page=2"))))
                .andExpect(content().string(containsString("cursor-not-allowed")));
    }

    @Test
    void mostrarFormulario_RetornaVistaConEventoVacioLugaresYCategorias() throws Exception {
        mockMvc.perform(get("/admin/eventos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(model().attributeExists("event", "venues", "categorias"))
                .andExpect(content().string(containsString("name=\"venueId\"")))
                .andExpect(content().string(containsString("name=\"categoryIds\"")));
    }

    @Test
    void mostrarFormularioEdicion_MuestraFechaEnFormatoIsoYRelacionesMarcadas() throws Exception {
        EventCreateDTO form = new EventCreateDTO();
        form.setId(21L);
        form.setNombre("Concierto de ROCK");
        form.setFecha(LocalDate.of(2026, 12, 20));
        form.setDescripcion("Noche de rock");
        form.setVenueId(5L);
        form.setCategoryIds(Set.of(8L));
        when(eventService.findFormById(21L)).thenReturn(form);

        mockMvc.perform(get("/admin/eventos/21/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                // <input type="date"> solo entiende yyyy-MM-dd
                .andExpect(content().string(containsString("value=\"2026-12-20\"")))
                .andExpect(content().string(matchesRegex("(?s).*<option value=\"5\" selected=\"selected\">.*")))
                .andExpect(content().string(matchesRegex("(?s).*value=\"8\"[^>]*checked=\"checked\".*")))
                // El id viaja oculto para el grupo OnUpdate
                .andExpect(content().string(matchesRegex("(?s).*type=\"hidden\"[^>]*name=\"id\" value=\"21\".*")));
    }

    @Test
    void mostrarFormularioEdicion_EventoInexistente_Retorna404() throws Exception {
        when(eventService.findFormById(9999L)).thenThrow(new ResourceNotFoundException("El evento con ID 9999 no existe"));

        mockMvc.perform(get("/admin/eventos/9999/editar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardar_EventoValido_RedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Conferencia Java")
                        .param("fecha", fechaFutura)
                        .param("descripcion", "Descripción de prueba")
                        .param("venueId", "1")
                        .param("categoryIds", "1", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"))
                .andExpect(flash().attribute("mensaje", "Evento registrado correctamente."));

        ArgumentCaptor<EventCreateDTO> evento = ArgumentCaptor.forClass(EventCreateDTO.class);
        verify(eventService).save(evento.capture());
        assertEquals(1L, evento.getValue().getVenueId());
        assertEquals(Set.of(1L, 8L), evento.getValue().getCategoryIds());
    }

    @Test
    void guardar_NombreVacio_RecargaFormularioConErrorYConservaLoEscrito() throws Exception {
        // Escenario 3
        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "")
                        .param("fecha", fechaFutura)
                        .param("descripcion", "Texto que no se debe perder")
                        .param("venueId", "5")
                        .param("categoryIds", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(model().attributeHasFieldErrors("event", "nombre"))
                .andExpect(model().attributeErrorCount("event", 2)) // NotBlank + Size del mismo campo
                // th:errors junto al campo y th:errorclass resaltándolo
                .andExpect(content().string(containsString("no debe estar vacío")))
                .andExpect(content().string(matchesRegex("(?s).*class=\"[^\"]*campo-invalido\" id=\"nombre\".*")))
                // Lo demás que escribió el coordinador se conserva
                .andExpect(content().string(containsString("Texto que no se debe perder</textarea>")))
                .andExpect(content().string(containsString("value=\"" + fechaFutura + "\"")))
                .andExpect(content().string(containsString("<option value=\"5\" selected=\"selected\">")))
                .andExpect(content().string(matchesRegex("(?s).*value=\"8\"[^>]*checked=\"checked\".*")))
                // Y vuelve a cargar las opciones de los desplegables
                .andExpect(model().attributeExists("venues", "categorias"));

        verify(eventService, never()).save(any());
    }

    @Test
    void guardar_FechaPasada_MuestraElMensajeDeNegocioDeNoPastEvents() throws Exception {
        // Escenario 4 desde el formulario
        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Conferencia Java")
                        .param("fecha", "2020-01-01")
                        .param("descripcion", "Descripción")
                        .param("venueId", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "fecha"))
                // th:errors muestra ambos: la regla de negocio y el mensaje genérico de @Future
                .andExpect(content().string(containsString("Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó")))
                .andExpect(content().string(containsString("debe ser una fecha futura")));
    }

    @Test
    void guardar_LugarEliminadoMientrasSeEditaba_MuestraErrorGlobal() throws Exception {
        when(eventService.save(any())).thenThrow(new ResourceNotFoundException("El lugar con ID 5 no existe"));

        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Conferencia Java")
                        .param("fecha", fechaFutura)
                        .param("descripcion", "Descripción")
                        .param("venueId", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(content().string(containsString("El lugar con ID 5 no existe")));
    }

    @Test
    void actualizar_Valido_RedirigeConMensaje() throws Exception {
        mockMvc.perform(post("/admin/eventos/21")
                        .param("id", "21")
                        .param("nombre", "Concierto de ROCK")
                        .param("fecha", fechaFutura)
                        .param("descripcion", "Editado")
                        .param("venueId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "Evento actualizado correctamente."));

        verify(eventService).update(eq(21L), any());
    }

    @Test
    void listar_ConMensajeFlash_MuestraConfirmacionEnLaVista() throws Exception {
        when(eventService.buscar(any(), any())).thenReturn(new SliceImpl<>(List.of()));

        mockMvc.perform(get("/admin/eventos")
                        .flashAttr("mensaje", "Evento registrado correctamente."))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(content().string(containsString("Evento registrado correctamente.")));
    }
}
