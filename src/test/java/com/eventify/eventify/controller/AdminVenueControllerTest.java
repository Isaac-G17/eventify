package com.eventify.eventify.controller;

import com.eventify.eventify.dto.VenueResponseDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.DuplicateResourceException;
import com.eventify.eventify.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        List<VenueResponseDTO> venues = List.of(
                new VenueResponseDTO(1L, "Auditorio Principal", "Calle 50 #20-10", "Bogotá", 300)
        );
        Page<VenueResponseDTO> pagina = new PageImpl<>(venues);
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
        Page<VenueResponseDTO> paginaVacia = new PageImpl<>(List.of());
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
                        .param("ciudad", "Bogotá")
                        .param("capacidad", "300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"))
                .andExpect(flash().attribute("mensaje", "Lugar registrado correctamente."));
    }

    @Test
    void guardar_DatosInvalidos_RecargaFormularioConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Auditorio Principal")
                        .param("direccion", "Calle 50 #20-10")
                        .param("ciudad", "")
                        .param("capacidad", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues-form"))
                .andExpect(model().attributeHasFieldErrors("venue", "ciudad", "capacidad"))
                .andExpect(content().string(containsString("debe ser de al menos 10 personas")))
                .andExpect(content().string(matchesRegex("(?s).*class=\"[^\"]*campo-invalido\" id=\"capacidad\".*")))
                .andExpect(content().string(containsString("value=\"Auditorio Principal\"")));

        verify(venueService, never()).save(any());
    }

    @Test
    void guardar_NombreDuplicado_MuestraErrorJuntoAlNombre() throws Exception {
        when(venueService.save(any())).thenThrow(new DuplicateResourceException("Ya existe un lugar con el nombre 'Teatro Municipal'"));

        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Teatro Municipal")
                        .param("direccion", "Plaza Central 12")
                        .param("ciudad", "Cali")
                        .param("capacidad", "300"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues-form"))
                .andExpect(model().attributeHasFieldErrorCode("venue", "nombre", "duplicado"))
                .andExpect(content().string(containsString("Ya existe un lugar con el nombre")));
    }

    @Test
    void eliminar_VenueConEventos_RedirigeConMensajeDeError() throws Exception {
        doThrow(new BusinessRuleViolationException("No se puede eliminar el lugar porque tiene eventos asociados"))
                .when(venueService).delete(1L);

        mockMvc.perform(post("/admin/venues/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/venues"))
                .andExpect(flash().attribute("error", "No se puede eliminar el lugar porque tiene eventos asociados"));
    }

    @Test
    void listar_ConErrorFlash_MuestraAlertaRoja() throws Exception {
        when(venueService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admin/venues").flashAttr("error", "No se puede eliminar el lugar porque tiene eventos asociados"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex("(?s).*bg-red-50[^>]*>\\s*No se puede eliminar el lugar.*")));
    }
}
