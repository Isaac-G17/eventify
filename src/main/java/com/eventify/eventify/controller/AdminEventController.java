package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventCreateDTO;
import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.exception.BusinessRuleViolationException;
import com.eventify.eventify.exception.ResourceNotFoundException;
import com.eventify.eventify.service.CategoryService;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
import com.eventify.eventify.validation.OnCreate;
import com.eventify.eventify.validation.OnUpdate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Controller
@RequestMapping("/admin/eventos")
public class AdminEventController {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public AdminEventController(EventService eventService, VenueService venueService, CategoryService categoryService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listar(Model model, @ModelAttribute("filtro") EventFilter filtro,
                         @PageableDefault(size = 8) Pageable pageable, HttpServletRequest request) {
        Slice<EventSummaryDTO> eventos;
        try {
            eventos = eventService.buscar(filtro, pageable);
        } catch (BusinessRuleViolationException e) {
            model.addAttribute("error", e.getMessage());
            eventos = new SliceImpl<>(List.of(), pageable, false);
        }
        model.addAttribute("eventos", eventos);
        model.addAttribute("urlAnterior", urlPagina(request, eventos.getNumber() - 1));
        model.addAttribute("urlSiguiente", urlPagina(request, eventos.getNumber() + 1));
        return "event-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event", new EventCreateDTO());
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findFormById(id));
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @PostMapping
    public String guardar(@Validated(OnCreate.class) @ModelAttribute("event") EventCreateDTO event, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                eventService.save(event);
                redirectAttributes.addFlashAttribute("mensaje", "Evento registrado correctamente.");
                return "redirect:/admin/eventos";
            } catch (ResourceNotFoundException e) {
                // El lugar o una categoría se eliminaron mientras se llenaba el formulario
                result.reject("relacionInexistente", e.getMessage());
            }
        }
        // Se vuelve a mostrar el formulario con los errores y con lo que el coordinador ya había escrito
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Validated(OnUpdate.class) @ModelAttribute("event") EventCreateDTO event,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                eventService.update(id, event);
                redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado correctamente.");
                return "redirect:/admin/eventos";
            } catch (ResourceNotFoundException | BusinessRuleViolationException e) {
                result.reject("actualizacionRechazada", e.getMessage());
            }
        }
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.delete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado correctamente.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/eventos";
    }

    private void agregarOpcionesFormulario(Model model) {
        model.addAttribute("venues", venueService.findAllOrdenados());
        model.addAttribute("categorias", categoryService.findAll());
    }

    /** URL de otra página conservando todos los filtros activos de la búsqueda actual. */
    private String urlPagina(HttpServletRequest request, int pagina) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        request.getParameterMap().forEach((nombre, valores) -> builder.queryParam(nombre, (Object[]) valores));
        return builder.replaceQueryParam("page", pagina).toUriString();
    }
}
