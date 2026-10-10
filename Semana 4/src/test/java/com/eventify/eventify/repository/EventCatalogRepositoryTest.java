package com.eventify.eventify.repository;

import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.model.Event;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Corre las 3 migraciones completas (V1, V2 y V3 con el seed masivo)
 * para validar el catálogo real tal como lo verá la aplicación.
 */
@DataJpaTest
class EventCatalogRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void flyway_SeedLoads200EventsWithRelations() {
        // Escenario 5: estructura relacional completa y 200 registros
        assertEquals(200, eventRepository.count());
        assertEquals(20, venueRepository.count());
        assertEquals(11, categoryRepository.count());
        Number vinculos = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM events_categories").getSingleResult();
        assertTrue(vinculos.longValue() >= 200);
    }

    @Test
    void buscarResumen_SinFiltros_ReturnsSliceOrderedByFechaDesc() {
        // Escenario 2: listado ordenado por fecha descendente
        Slice<EventSummaryDTO> slice = eventRepository.buscarResumen(null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(10, slice.getContent().size());
        assertTrue(slice.hasNext());
        List<LocalDate> fechas = slice.getContent().stream().map(EventSummaryDTO::fecha).toList();
        for (int i = 1; i < fechas.size(); i++) {
            assertFalse(fechas.get(i).isAfter(fechas.get(i - 1)), "Las fechas deben estar en orden descendente");
        }
    }

    @Test
    void buscarResumen_CiudadYCategoriaParcialesSinMayusculas_EncuentraConciertoDeRock() {
        // Escenario 6: "bog" encuentra Bogotá y "rock" encuentra la categoría Rock
        Slice<EventSummaryDTO> slice = eventRepository.buscarResumen("bog", "rock", null, null, null, PageRequest.of(0, 50));

        assertTrue(slice.getContent().stream().anyMatch(e -> e.nombre().equals("Concierto de ROCK")));
        assertTrue(slice.getContent().stream().allMatch(e -> e.ciudad().equals("Bogotá")));
    }

    @Test
    void buscarResumen_EnMayusculas_TambienEncuentra() {
        Slice<EventSummaryDTO> slice = eventRepository.buscarResumen("BOGOTÁ", "ROCK", null, null, null, PageRequest.of(0, 50));

        assertTrue(slice.getContent().stream().anyMatch(e -> e.nombre().equals("Concierto de ROCK")));
    }

    @Test
    void buscarResumen_CapacidadYRangoDeFechas_FiltraCorrectamente() {
        LocalDate desde = LocalDate.of(2026, 11, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);

        Slice<EventSummaryDTO> slice = eventRepository.buscarResumen(null, null, 1000, desde, hasta, PageRequest.of(0, 200));

        assertFalse(slice.getContent().isEmpty());
        slice.getContent().forEach(e -> {
            assertFalse(e.fecha().isBefore(desde));
            assertFalse(e.fecha().isAfter(hasta));
        });
        // Todos los lugares con capacidad >= 1000 del seed
        List<String> lugaresGrandes = List.of("Estadio Cubierto Los Andes", "Parque de Eventos La Colina", "Coliseo de Ferias", "Jardín Botánico - Explanada Sur");
        assertTrue(slice.getContent().stream().allMatch(e -> lugaresGrandes.contains(e.lugar())));
    }

    @Test
    void findById_CargaLugarYCategoriasEnUnaSolaConsulta() {
        Long id = idDe("Concierto de ROCK");
        entityManager.clear();

        Event evento = eventRepository.findById(id).orElseThrow();

        // @EntityGraph: las relaciones ya vienen cargadas, no quedan proxies perezosos
        assertTrue(Hibernate.isInitialized(evento.getVenue()));
        assertTrue(Hibernate.isInitialized(evento.getCategorias()));
        assertEquals("Bogotá", evento.getVenue().getCiudad());
        assertEquals(2, evento.getCategorias().size());
    }

    @Test
    void softDelete_OcultaElEventoPeroConservaElRegistro() {
        Long id = idDe("Concierto de ROCK");

        eventRepository.softDelete(id);

        assertTrue(eventRepository.findById(id).isEmpty());
        assertEquals(199, eventRepository.count());
        assertTrue(eventRepository.buscarResumen("bog", "rock", null, null, null, PageRequest.of(0, 50))
                .getContent().stream().noneMatch(e -> e.id().equals(id)));
        // El registro sigue físicamente en la tabla, marcado como inactivo
        Object activo = entityManager.createNativeQuery("SELECT activo FROM events WHERE id = :id")
                .setParameter("id", id).getSingleResult();
        assertEquals(Boolean.FALSE, activo);
    }

    @Test
    void save_ConCategorias_PersisteLosVinculosEnCascada() {
        Event evento = new Event(null, "Evento Nuevo", LocalDate.now().plusMonths(1), "Descripción");
        evento.setVenue(venueRepository.findAll().get(0));
        evento.setCategorias(new HashSet<>(categoryRepository.findAll().subList(0, 2)));

        Event guardado = eventRepository.saveAndFlush(evento);

        Number vinculos = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM events_categories WHERE event_id = :id")
                .setParameter("id", guardado.getId()).getSingleResult();
        assertEquals(2, vinculos.intValue());
        assertTrue(guardado.getActivo());
    }

    private Long idDe(String nombre) {
        return eventRepository.findByNombreContaining(nombre).stream()
                .filter(e -> e.getNombre().equals(nombre))
                .findFirst().orElseThrow().getId();
    }
}
