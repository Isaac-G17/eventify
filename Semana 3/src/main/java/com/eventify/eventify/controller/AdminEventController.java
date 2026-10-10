package com.eventify.eventify.controller;

import com.eventify.eventify.model.Event;
import com.eventify.eventify.service.EventService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/eventos")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String listar(Model model, @PageableDefault(size = 8) Pageable pageable) {
        model.addAttribute("eventos", eventService.findAll(pageable));
        return "event-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "event-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {
        eventService.save(event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento registrado correctamente.");
        return "redirect:/admin/eventos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Event event, RedirectAttributes redirectAttributes) {
        eventService.update(id, event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado correctamente.");
        return "redirect:/admin/eventos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.delete(id);
        redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado correctamente.");
        return "redirect:/admin/eventos";
    }
}
