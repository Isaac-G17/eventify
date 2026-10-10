package com.eventify.eventify.validation;

import jakarta.validation.groups.Default;

/**
 * Grupo de validación para la actualización (PUT). Hereda de Default, así que al validar
 * con este grupo también se aplican todas las reglas comunes (sin grupo explícito).
 */
public interface OnUpdate extends Default {
}
