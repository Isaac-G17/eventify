package com.eventify.eventify.docs;

/**
 * Cuerpos de ejemplo (RFC 7807) que Swagger muestra en cada código de error documentado.
 * Coinciden con lo que construye GlobalExceptionHandler.
 */
public final class ProblemDetailExamples {

    private ProblemDetailExamples() {
    }

    public static final String MEDIA_TYPE = "application/problem+json";

    public static final String VALIDACION_400 = """
            {
              "type": "https://api.eventify.com/errores/validacion",
              "title": "Datos de entrada inválidos",
              "status": 400,
              "detail": "La petición tiene 2 campo(s) con errores de validación",
              "instance": "/api/events",
              "timestamp": "2026-10-10T15:30:00Z",
              "errores": {
                "nombre": "no debe estar vacío",
                "fecha": "Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó"
              },
              "detalles": [
                {"campo": "nombre", "codigo": "NotBlank", "mensaje": "no debe estar vacío"},
                {"campo": "fecha", "codigo": "NoPastEvents", "mensaje": "Política de Eventify: no se pueden registrar eventos con una fecha que ya pasó"},
                {"campo": "fecha", "codigo": "Future", "mensaje": "debe ser una fecha futura"}
              ]
            }""";

    public static final String EVENTO_404 = """
            {
              "type": "https://api.eventify.com/errores/recurso-no-encontrado",
              "title": "Recurso no encontrado",
              "status": 404,
              "detail": "El evento con ID 9999 no existe",
              "instance": "/api/events/9999",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";

    public static final String LUGAR_404 = """
            {
              "type": "https://api.eventify.com/errores/recurso-no-encontrado",
              "title": "Recurso no encontrado",
              "status": 404,
              "detail": "El lugar con ID 9999 no existe",
              "instance": "/api/venues/9999",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";

    public static final String RELACION_404 = """
            {
              "type": "https://api.eventify.com/errores/recurso-no-encontrado",
              "title": "Recurso no encontrado",
              "status": 404,
              "detail": "El lugar con ID 99 no existe",
              "instance": "/api/events",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";

    public static final String DUPLICADO_409 = """
            {
              "type": "https://api.eventify.com/errores/recurso-duplicado",
              "title": "Recurso duplicado",
              "status": 409,
              "detail": "Ya existe un lugar con el nombre 'Teatro Municipal'",
              "instance": "/api/venues",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";

    public static final String REGLA_NEGOCIO_422 = """
            {
              "type": "https://api.eventify.com/errores/regla-de-negocio",
              "title": "Regla de negocio violada",
              "status": 422,
              "detail": "No se puede eliminar el lugar porque tiene eventos asociados",
              "instance": "/api/venues/1",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";

    public static final String ERROR_500 = """
            {
              "type": "https://api.eventify.com/errores/error-interno",
              "title": "Error interno del servidor",
              "status": 500,
              "detail": "Ocurrió un error inesperado. Si el problema persiste, contacte al equipo de soporte de Eventify",
              "instance": "/api/events",
              "timestamp": "2026-10-10T15:30:00Z"
            }""";
}
