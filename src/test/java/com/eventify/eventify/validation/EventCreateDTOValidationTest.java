package com.eventify.eventify.validation;

import com.eventify.eventify.dto.EventCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas de Bean Validation del DTO de entrada de eventos (antes vivían como if/throw en EventService).
 */
class EventCreateDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private EventCreateDTO dto;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        dto = new EventCreateDTO();
        dto.setNombre("Conferencia Java");
        dto.setFecha(LocalDate.now().plusMonths(1));
        dto.setDescripcion("Charla técnica de backend");
        dto.setVenueId(1L);
    }

    @Test
    void validDto_HasNoViolations() {
        assertTrue(validator.validate(dto, OnCreate.class).isEmpty());
    }

    @Test
    void emptyName_FailsWithNotBlankMessage() {
        dto.setNombre("   ");
        assertTrue(mensajes(dto, OnCreate.class).get("nombre").contains("no debe estar vacío"));
    }

    @Test
    void nullName_Fails() {
        dto.setNombre(null);
        assertTrue(mensajes(dto, OnCreate.class).containsKey("nombre"));
    }

    @Test
    void shortName_FailsWithSizeMessage() {
        dto.setNombre("AB");
        assertEquals(Set.of("debe tener entre 3 y 100 caracteres"), mensajes(dto, OnCreate.class).get("nombre"));
    }

    @Test
    void nullFecha_Fails() {
        dto.setFecha(null);
        assertEquals(Set.of("es obligatoria"), mensajes(dto, OnCreate.class).get("fecha"));
    }

    @Test
    void pastFecha_FailsWithBusinessRuleAndFutureMessages() {
        // Escenario 4: la regla de negocio (@NoPastEvents) se distingue del mensaje genérico de @Future
        dto.setFecha(LocalDate.of(2020, 1, 1));
        Set<String> codigos = validator.validate(dto, OnCreate.class).stream()
                .filter(v -> v.getPropertyPath().toString().equals("fecha"))
                .map(v -> v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .collect(Collectors.toSet());
        assertEquals(Set.of("NoPastEvents", "Future"), codigos);
        assertTrue(mensajes(dto, OnCreate.class).get("fecha")
                .contains("Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó"));
    }

    @Test
    void todayFecha_FailsOnlyWithFuture() {
        // Hoy no es "pasado" para @NoPastEvents, pero tampoco es futuro para @Future
        dto.setFecha(LocalDate.now());
        assertEquals(Set.of("debe ser una fecha futura"), mensajes(dto, OnCreate.class).get("fecha"));
    }

    @Test
    void emptyDescripcion_Fails() {
        dto.setDescripcion("   ");
        assertTrue(mensajes(dto, OnCreate.class).containsKey("descripcion"));
    }

    @Test
    void descripcionTooLong_Fails() {
        dto.setDescripcion("a".repeat(501));
        assertEquals(Set.of("no puede superar 500 caracteres"), mensajes(dto, OnCreate.class).get("descripcion"));
    }

    @Test
    void withoutVenue_Fails() {
        dto.setVenueId(null);
        assertEquals(Set.of("debe seleccionar un lugar"), mensajes(dto, OnCreate.class).get("venueId"));
    }

    @Test
    void invalidEmail_Fails() {
        dto.setEmailOrganizador("no-es-un-correo");
        assertEquals(Set.of("debe ser un correo electrónico válido"), mensajes(dto, OnCreate.class).get("emailOrganizador"));
    }

    @Test
    void validEmail_Passes() {
        dto.setEmailOrganizador("organizador@eventify.com");
        assertTrue(validator.validate(dto, OnCreate.class).isEmpty());
    }

    @Test
    void groupOnCreate_RejectsId() {
        dto.setId(5L);
        assertEquals(Set.of("no debe enviarse al crear un evento"), mensajes(dto, OnCreate.class).get("id"));
    }

    @Test
    void groupOnUpdate_RequiresId() {
        assertEquals(Set.of("es obligatorio al actualizar un evento"), mensajes(dto, OnUpdate.class).get("id"));
        dto.setId(5L);
        assertTrue(validator.validate(dto, OnUpdate.class).isEmpty());
    }

    @Test
    void groupOnUpdate_StillAppliesCommonRules() {
        // OnUpdate hereda de Default: las reglas sin grupo también se validan al actualizar
        dto.setId(5L);
        dto.setNombre("");
        assertTrue(mensajes(dto, OnUpdate.class).containsKey("nombre"));
    }

    private Map<String, Set<String>> mensajes(EventCreateDTO evento, Class<?> grupo) {
        Set<ConstraintViolation<EventCreateDTO>> violaciones = validator.validate(evento, grupo);
        return violaciones.stream().collect(Collectors.groupingBy(v -> v.getPropertyPath().toString(),
                Collectors.mapping(ConstraintViolation::getMessage, Collectors.toSet())));
    }
}
