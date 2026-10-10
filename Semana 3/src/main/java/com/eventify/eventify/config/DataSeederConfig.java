package com.eventify.eventify.config;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeederConfig {

    @Bean
    @ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true",matchIfMissing = true)
    public boolean seedInitialData(EventService eventService, VenueService venueService){
        venueService.save(new Venue(null, "Centro de Convenciones Principal", "Av. El Sol 123", 500));
        venueService.save(new Venue(null, "Auditorio Tecnológico", "Calle Innovación 456", 150));
        venueService.save(new Venue(null, "Teatro Municipal", "Plaza Central 12", 300));
        venueService.save(new Venue(null, "Sala de Conferencias Norte", "Av. Libertadores 789", 80));
        venueService.save(new Venue(null, "Estadio Cubierto Los Andes", "Carrera 45 #10-20", 1200));
        venueService.save(new Venue(null, "Salón Gastronómico El Fogón", "Calle Sabor 34", 60));
        venueService.save(new Venue(null, "Galería de Arte Contemporáneo", "Av. Cultura 200", 100));
        venueService.save(new Venue(null, "Club Nocturno Eclipse", "Zona Rosa, Calle 45", 250));
        venueService.save(new Venue(null, "Parque de Eventos La Colina", "Km 5 Vía al Norte", 2000));
        venueService.save(new Venue(null, "Biblioteca Central - Sala Multiusos", "Av. del Saber 88", 120));
        venueService.save(new Venue(null, "Hotel Gran Plaza - Salón Esmeralda", "Carrera 7 #55-30", 400));
        venueService.save(new Venue(null, "Centro Cultural Municipal", "Calle Real 15", 350));
        venueService.save(new Venue(null, "Coliseo de Ferias", "Av. Exposición 500", 3000));
        venueService.save(new Venue(null, "Café Concierto La Bohemia", "Calle 8 #12-40", 90));
        venueService.save(new Venue(null, "Auditorio Universidad del Valle", "Ciudad Universitaria s/n", 600));
        venueService.save(new Venue(null, "Terraza Skyline Rooftop", "Torre Empresarial, Piso 20", 180));
        venueService.save(new Venue(null, "Polideportivo Miraflores", "Av. Deportiva 300", 800));
        venueService.save(new Venue(null, "Casa de la Cultura", "Calle Artesanos 22", 140));
        venueService.save(new Venue(null, "Centro de Negocios Andino", "Av. Financiera 900", 250));
        venueService.save(new Venue(null, "Jardín Botánico - Explanada Sur", "Vía al Jardín Km 2", 1000));

        eventService.save(new Event(null, "Conferencia Tech 2026", LocalDate.of(2026, 10, 15), "Encuentro anual de desarrollo de software y nuevas tecnologías"));
        eventService.save(new Event(null, "Workshop Spring Boot", LocalDate.of(2026, 11, 20), "Taller práctico de backend con Spring Boot y JPA"));
        eventService.save(new Event(null, "Festival de Jazz Internacional", LocalDate.of(2026, 10, 25), "Presentación de bandas nacionales e internacionales de jazz"));
        eventService.save(new Event(null, "Feria Gastronómica de Otoño", LocalDate.of(2026, 11, 5), "Degustación de platos típicos de más de veinte restaurantes locales"));
        eventService.save(new Event(null, "Exposición de Arte Moderno", LocalDate.of(2026, 10, 30), "Muestra colectiva de artistas emergentes de la región"));
        eventService.save(new Event(null, "Maratón Ciudad Saludable", LocalDate.of(2026, 12, 1), "Carrera de 10K y 21K por las principales avenidas de la ciudad"));
        eventService.save(new Event(null, "Noche de Stand Up Comedy", LocalDate.of(2026, 11, 8), "Show de comedia con humoristas invitados"));
        eventService.save(new Event(null, "Conferencia de Inteligencia Artificial", LocalDate.of(2026, 11, 18), "Charlas sobre machine learning y aplicaciones de IA en la industria"));
        eventService.save(new Event(null, "Concierto Sinfónico de Fin de Año", LocalDate.of(2026, 12, 15), "Presentación de la orquesta filarmónica con repertorio navideño"));
        eventService.save(new Event(null, "Torneo Municipal de Ajedrez", LocalDate.of(2026, 11, 12), "Competencia abierta para jugadores aficionados y federados"));
        eventService.save(new Event(null, "Feria del Libro Independiente", LocalDate.of(2026, 10, 22), "Exhibición y venta de editoriales independientes y autores locales"));
        eventService.save(new Event(null, "Congreso de Emprendimiento", LocalDate.of(2026, 11, 27), "Charlas y paneles sobre innovación y creación de startups"));
        eventService.save(new Event(null, "Noche de Cine al Aire Libre", LocalDate.of(2026, 10, 18), "Proyección de clásicos del cine bajo las estrellas"));
        eventService.save(new Event(null, "Encuentro de Food Trucks", LocalDate.of(2026, 11, 2), "Variedad de comida callejera de distintos países"));
        eventService.save(new Event(null, "Taller de Fotografía Urbana", LocalDate.of(2026, 10, 29), "Curso práctico sobre técnicas de fotografía en entornos urbanos"));
        eventService.save(new Event(null, "Gran Final de Talentos Locales", LocalDate.of(2026, 12, 6), "Competencia final de música, baile y otras disciplinas artísticas"));
        eventService.save(new Event(null, "Rueda de Negocios Regional", LocalDate.of(2026, 11, 14), "Espacio de networking entre empresas y proveedores de la región"));
        eventService.save(new Event(null, "Festival de Cerveza Artesanal", LocalDate.of(2026, 10, 24), "Muestra de cervecerías artesanales locales e internacionales"));
        eventService.save(new Event(null, "Charla de Sostenibilidad Ambiental", LocalDate.of(2026, 11, 21), "Panel de expertos sobre energías renovables y cuidado ambiental"));
        eventService.save(new Event(null, "Concierto de Rock Alternativo", LocalDate.of(2026, 12, 10), "Presentación de bandas emergentes del género rock alternativo"));

        return true;
    }
}
