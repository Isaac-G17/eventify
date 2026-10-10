package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.CategoryService;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
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
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        when(venueService.findAllOrdenados()).thenReturn(List.of(new Venue(1L, "Auditorio Principal", "Calle 50", "Bogotá", 300)));
        when(categoryService.findAll()).thenReturn(List.of(new Category(1L, "Conciertos", "Música en vivo")));

        mockMvc.perform(get("/admin/eventos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(model().attributeExists("event", "venues", "categorias"))
                .andExpect(content().string(containsString("name=\"venue.id\"")))
                .andExpect(content().string(containsString("name=\"categoriaIds\"")));
    }

    @Test
    void mostrarFormularioEdicion_MuestraFechaEnFormatoIsoYRelacionesMarcadas() throws Exception {
        Venue venue = new Venue(5L, "Estadio Cubierto Los Andes", "Carrera 45", "Bogotá", 1200);
        Category rock = new Category(8L, "Rock", "Género rock");
        Event evento = new Event(21L, "Concierto de ROCK", LocalDate.of(2026, 12, 20), "Noche de rock");
        evento.setVenue(venue);
        evento.getCategorias().add(rock);
        when(eventService.findById(21L)).thenReturn(evento);
        when(venueService.findAllOrdenados()).thenReturn(List.of(venue));
        when(categoryService.findAll()).thenReturn(List.of(rock, new Category(1L, "Conciertos", "Música en vivo")));

        mockMvc.perform(get("/admin/eventos/21/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                // <input type="date"> solo entiende yyyy-MM-dd
                .andExpect(content().string(containsString("value=\"2026-12-20\"")))
                .andExpect(content().string(matchesRegex("(?s).*value=\"5\"\\s+selected=\"selected\".*")))
                .andExpect(content().string(matchesRegex("(?s).*value=\"8\"\\s+class=\"accent-blue-950\" checked=\"checked\".*")));
    }

    @Test
    void guardar_EventoValido_RedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Conferencia Java")
                        .param("fecha", "2026-10-10")
                        .param("descripcion", "Descripción de prueba")
                        .param("venue.id", "1")
                        .param("categoriaIds", "1", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"))
                .andExpect(flash().attribute("mensaje", "Evento registrado correctamente."));

        ArgumentCaptor<Event> evento = ArgumentCaptor.forClass(Event.class);
        verify(eventService).save(evento.capture());
        assertEquals(1L, evento.getValue().getVenue().getId());
        assertEquals(java.util.Set.of(1L, 8L),
                evento.getValue().getCategorias().stream().map(Category::getId).collect(Collectors.toSet()));
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
