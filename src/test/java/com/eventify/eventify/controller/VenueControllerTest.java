package com.eventify.eventify.controller;

import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.DuplicateResourceException;
import com.eventify.eventify.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
    void create_CapacidadMenorAlMinimo_Retorna400() throws Exception {
        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Lugar Nuevo", "direccion": "Calle 1", "ciudad": "Cali", "capacidad": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.capacidad").value("debe ser de al menos 10 personas"));
    }

    @Test
    void create_NombreDuplicado_Retorna409ConProblemDetail() throws Exception {
        when(venueService.save(any())).thenThrow(new DuplicateResourceException("Ya existe un lugar con el nombre 'Teatro Municipal'"));

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Teatro Municipal", "direccion": "Plaza Central 12", "ciudad": "Cali", "capacidad": 300}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Recurso duplicado"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.instance").value("/api/venues"));
    }

    @Test
    void delete_ConEventosAsociados_Retorna422() throws Exception {
        doThrow(new BusinessRuleViolationException("No se puede eliminar el lugar porque tiene eventos asociados"))
                .when(venueService).delete(1L);

        mockMvc.perform(delete("/api/venues/1"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regla de negocio violada"))
                .andExpect(jsonPath("$.detail").value("No se puede eliminar el lugar porque tiene eventos asociados"));
    }
}
