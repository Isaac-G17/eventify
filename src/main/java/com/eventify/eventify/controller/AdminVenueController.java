package com.eventify.eventify.controller;

import com.eventify.eventify.model.Venue;
import com.eventify.eventify.service.VenueService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public String listar(Model model, @PageableDefault(size = 8) Pageable pageable) {
        model.addAttribute("venues", venueService.findAll(pageable));
        return "venues-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.findById(id));
        return "venues-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Venue venue) {
        venueService.save(venue);
        return "redirect:/admin/venues";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Venue venue) {
        venueService.update(id, venue);
        return "redirect:/admin/venues";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        venueService.delete(id);
        return "redirect:/admin/venues";
    }
}