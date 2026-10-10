package com.eventify.eventify.controller;

import com.eventify.eventify.dto.EventFilter;
import com.eventify.eventify.dto.EventSummaryDTO;
import com.eventify.eventify.model.Category;
import com.eventify.eventify.model.Event;
import com.eventify.eventify.service.CategoryService;
import com.eventify.eventify.service.EventService;
import com.eventify.eventify.service.VenueService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        Slice<EventSummaryDTO> eventos = eventService.buscar(filtro, pageable);
        model.addAttribute("eventos", eventos);
        model.addAttribute("urlAnterior", urlPagina(request, eventos.getNumber() - 1));
        model.addAttribute("urlSiguiente", urlPagina(request, eventos.getNumber() + 1));
        return "event-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event", new Event());
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        agregarOpcionesFormulario(model);
        return "event-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute Event event,
                          @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                          RedirectAttributes redirectAttributes) {
        asignarCategorias(event, categoriaIds);
        eventService.save(event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento registrado correctamente.");
        return "redirect:/admin/eventos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Event event,
                             @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                             RedirectAttributes redirectAttributes) {
        asignarCategorias(event, categoriaIds);
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

    private void agregarOpcionesFormulario(Model model) {
        model.addAttribute("venues", venueService.findAllOrdenados());
        model.addAttribute("categorias", categoryService.findAll());
    }

    private void asignarCategorias(Event event, List<Long> categoriaIds) {
        if (categoriaIds != null) {
            categoriaIds.forEach(categoriaId -> event.getCategorias().add(Category.conId(categoriaId)));
        }
    }

    /** URL de otra página conservando todos los filtros activos de la búsqueda actual. */
    private String urlPagina(HttpServletRequest request, int pagina) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        request.getParameterMap().forEach((nombre, valores) -> builder.queryParam(nombre, (Object[]) valores));
        return builder.replaceQueryParam("page", pagina).toUriString();
    }
}
