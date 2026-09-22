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

        eventService.save(new Event(null, "Conferencia Tech 2026", LocalDate.of(2026, 10, 15), "Encuentro anual de desarrollo"));
        eventService.save(new Event(null, "Workshop Spring Boot", LocalDate.of(2026, 11, 20), "Taller práctico de backend"));

        return true;
    }
}
