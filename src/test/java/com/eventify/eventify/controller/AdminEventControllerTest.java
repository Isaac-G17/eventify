package com.eventify.eventify.controller;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void listar_ConEventos_RetornaVistaConModelo() throws Exception {
        List<Event> eventos = List.of(
                new Event(1L, "Conferencia Java", LocalDate.of(2026, 10, 10), "Descripción")
        );
        Page<Event> pagina = new PageImpl<>(eventos);
        when(eventService.findAll(any())).thenReturn(pagina);

        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(model().attributeExists("eventos"))
                .andExpect(model().attribute("eventos", pagina));
    }

    @Test
    void listar_SinEventos_RetornaVistaConListaVacia() throws Exception {
        Page<Event> paginaVacia = new PageImpl<>(List.of());
        when(eventService.findAll(any())).thenReturn(paginaVacia);

        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(model().attribute("eventos", paginaVacia));
    }

    @Test
    void mostrarFormulario_RetornaVistaConEventoVacio() throws Exception {
        mockMvc.perform(get("/admin/eventos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(model().attributeExists("event"));
    }

    @Test
    void guardar_EventoValido_RedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Conferencia Java")
                        .param("fecha", "2026-10-10")
                        .param("descripcion", "Descripción de prueba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));
    }
}