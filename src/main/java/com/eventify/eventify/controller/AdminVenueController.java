package com.eventify.eventify.controller;

import com.eventify.eventify.dto.VenueCreateDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.DuplicateResourceException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.service.VenueService;
import com.eventify.eventify.validation.OnCreate;
import com.eventify.eventify.validation.OnUpdate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("venue", new VenueCreateDTO());
        return "venues-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.findFormById(id));
        return "venues-form";
    }

    @PostMapping
    public String guardar(@Validated(OnCreate.class) @ModelAttribute("venue") VenueCreateDTO venue, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                venueService.save(venue);
                redirectAttributes.addFlashAttribute("mensaje", "Lugar registrado correctamente.");
                return "redirect:/admin/venues";
            } catch (DuplicateResourceException e) {
                result.rejectValue("nombre", "duplicado", e.getMessage());
            }
        }
        // Se vuelve a mostrar el formulario con los errores y con lo que el coordinador ya había escrito
        return "venues-form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Validated(OnUpdate.class) @ModelAttribute("venue") VenueCreateDTO venue,
                             BindingResult result, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                venueService.update(id, venue);
                redirectAttributes.addFlashAttribute("mensaje", "Lugar actualizado correctamente.");
                return "redirect:/admin/venues";
            } catch (DuplicateResourceException e) {
                result.rejectValue("nombre", "duplicado", e.getMessage());
            } catch (ResourceNotFoundException | BusinessRuleViolationException e) {
                result.reject("actualizacionRechazada", e.getMessage());
            }
        }
        return "venues-form";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            venueService.delete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Lugar eliminado correctamente.");
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/venues";
    }
}
