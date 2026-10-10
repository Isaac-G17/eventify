package com.eventify.eventify.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class NoPastEventsValidatorTest {

    private final NoPastEventsValidator validator = new NoPastEventsValidator();

    @Test
    void pastDate_IsInvalid() {
        assertFalse(validator.isValid(LocalDate.now().minusDays(1), null));
    }

    @Test
    void today_IsValid() {
        assertTrue(validator.isValid(LocalDate.now(), null));
    }

    @Test
    void futureDate_IsValid() {
        assertTrue(validator.isValid(LocalDate.now().plusDays(1), null));
    }

    @Test
    void nullDate_IsValid_BecauseNotNullHandlesIt() {
        assertTrue(validator.isValid(null, null));
    }
}
