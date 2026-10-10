package com.eventify.eventify.validation;

import com.eventify.eventify.dto.VenueCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas de Bean Validation del DTO de entrada de lugares (antes vivían como if/throw en VenueService).
 */
class VenueCreateDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private VenueCreateDTO dto;

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
        dto = new VenueCreateDTO();
        dto.setNombre("Auditorio Principal");
        dto.setDireccion("Calle 50 #20-10");
        dto.setCiudad("Bogotá");
        dto.setCapacidad(300);
    }

    @Test
    void validDto_HasNoViolations() {
        assertTrue(validator.validate(dto, OnCreate.class).isEmpty());
    }

    @Test
    void emptyName_Fails() {
        dto.setNombre("");
        assertTrue(mensajes(OnCreate.class).get("nombre").contains("no debe estar vacío"));
    }

    @Test
    void nullName_Fails() {
        dto.setNombre(null);
        assertTrue(mensajes(OnCreate.class).containsKey("nombre"));
    }

    @Test
    void emptyDireccion_Fails() {
        dto.setDireccion("   ");
        assertEquals(Set.of("no debe estar vacía"), mensajes(OnCreate.class).get("direccion"));
    }

    @Test
    void emptyCiudad_Fails() {
        dto.setCiudad("  ");
        assertEquals(Set.of("no debe estar vacía"), mensajes(OnCreate.class).get("ciudad"));
    }

    @Test
    void nullCapacidad_Fails() {
        dto.setCapacidad(null);
        assertEquals(Set.of("es obligatoria"), mensajes(OnCreate.class).get("capacidad"));
    }

    @Test
    void negativeCapacidad_Fails() {
        dto.setCapacidad(-5);
        assertEquals(Set.of("debe ser de al menos 10 personas"), mensajes(OnCreate.class).get("capacidad"));
    }

    @Test
    void zeroCapacidad_Fails() {
        dto.setCapacidad(0);
        assertTrue(mensajes(OnCreate.class).containsKey("capacidad"));
    }

    @Test
    void capacidadOverMax_Fails() {
        dto.setCapacidad(100001);
        assertEquals(Set.of("no puede superar 100000 personas"), mensajes(OnCreate.class).get("capacidad"));
    }

    @Test
    void groupOnCreate_RejectsId_AndOnUpdate_RequiresIt() {
        dto.setId(1L);
        assertTrue(mensajes(OnCreate.class).containsKey("id"));
        assertTrue(validator.validate(dto, OnUpdate.class).isEmpty());
        dto.setId(null);
        assertEquals(Set.of("es obligatorio al actualizar un lugar"), mensajes(OnUpdate.class).get("id"));
    }

    private Map<String, Set<String>> mensajes(Class<?> grupo) {
        Set<ConstraintViolation<VenueCreateDTO>> violaciones = validator.validate(dto, grupo);
        return violaciones.stream().collect(Collectors.groupingBy(v -> v.getPropertyPath().toString(),
                Collectors.mapping(ConstraintViolation::getMessage, Collectors.toSet())));
    }
}
