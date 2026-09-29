package com.eventify.eventify.controller;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final EventService eventService;
    private final VenueService venueService;

    public HomeController(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(name = "pageEventos", defaultValue = "0") int pageEventos,
                       @RequestParam(name = "pageLugares", defaultValue = "0") int pageLugares) {
        model.addAttribute("eventos", eventService.findAll(PageRequest.of(pageEventos, 6)));
        model.addAttribute("lugares", venueService.findAll(PageRequest.of(pageLugares, 6)));
        model.addAttribute("event", new Event());
        model.addAttribute("venue", new Venue());
        return "index";
    }
}