package com.eventify.eventify.controller;

import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVenueController.class)
class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
    void listar_ConVenues_RetornaVistaConModelo() throws Exception {
        // Arrange
        List<Venue> venues = List.of(
                new Venue(1L, "Auditorio Principal", "Calle 50 #20-10", 300)
        );
        Page<Venue> pagina = new PageImpl<>(venues);
        when(venueService.findAll(any())).thenReturn(pagina);

        // Act & Assert
        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues-list"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", pagina));
    }

    @Test
    void listar_SinVenues_RetornaVistaConListaVacia() throws Exception {
        // Arrange
        Page<Venue> paginaVacia = new PageImpl<>(List.of());
        when(venueService.findAll(any())).thenReturn(paginaVacia);

        // Act & Assert
        mockMvc.perform(get("/admin/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues-list"))
                .andExpect(model().attribute("venues", paginaVacia));
    }


    @Test
    void mostrarFormulario_RetornaVistaConVenueVacio() throws Exception {
        mockMvc.perform(get("/admin/venues/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues-form"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    void guardar_VenueValido_RedirigeAlListado() throws Exception {
        mockMvc.perform(post("/admin/venues")
                .param("nombre", "Auditorio Principal")
                        .param("direccion", "Calle 50 #20-10")
                        .param("capacidad", "300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"));
    }
}
