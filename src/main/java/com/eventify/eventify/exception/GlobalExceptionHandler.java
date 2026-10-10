package com.eventify.eventify.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.*;

/**
 * Manejo centralizado de errores de la API con el estándar RFC 7807 (Problem Details).
 * Solo aplica a los @RestController: el panel Thymeleaf muestra sus errores dentro del formulario.
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TIPOS = "https://api.eventify.com/errores/";

    /** Cuando un campo tiene varios errores, el mapa muestra el más útil: primero "falta el dato", luego la regla de negocio. */
    private static final List<String> PRIORIDAD_CODIGOS = List.of("NotNull", "NotBlank", "NotEmpty", "Null", "NoPastEvents");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> erroresCampo = new ArrayList<>(ex.getBindingResult().getFieldErrors());
        erroresCampo.sort(Comparator.comparingInt(GlobalExceptionHandler::prioridad));

        Map<String, String> errores = new LinkedHashMap<>();
        List<Map<String, String>> detalles = new ArrayList<>();
        for (FieldError error : erroresCampo) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
            detalles.add(Map.of("campo", error.getField(), "codigo", String.valueOf(error.getCode()), "mensaje", String.valueOf(error.getDefaultMessage())));
        }
        ex.getBindingResult().getGlobalErrors().forEach(error -> errores.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail problem = problema(HttpStatus.BAD_REQUEST, "validacion", "Datos de entrada inválidos",
                "La petición tiene " + errores.size() + " campo(s) con errores de validación", request);
        problem.setProperty("errores", errores);
        problem.setProperty("detalles", detalles);
        return problem;
    }

    /** Validaciones que se disparan fuera del @RequestBody (ej. al persistir la entidad). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

        ProblemDetail problem = problema(HttpStatus.BAD_REQUEST, "validacion", "Datos de entrada inválidos",
                "La petición tiene " + errores.size() + " campo(s) con errores de validación", request);
        problem.setProperty("errores", errores);
        return problem;
    }

    /** JSON mal formado o con un tipo incorrecto (ej. "fecha": "20-12-2026"): error de formato, no de negocio. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "formato-invalido", "Formato de petición inválido",
                "El cuerpo de la petición no es un JSON válido o algún campo tiene un formato incorrecto (las fechas van como yyyy-MM-dd)", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "formato-invalido", "Formato de petición inválido",
                "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue(), request);
    }

    /** 404, 409 y 422: cada excepción de dominio ya sabe su código, título y tipo. */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setType(ex.getTipo());
        problem.setTitle(ex.getTitulo());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Red de seguridad: nunca se expone el stack trace ni el mensaje interno al cliente. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        // Errores propios de Spring MVC (ruta inexistente, método no permitido...) ya traen su ProblemDetail y su código
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problem = errorResponse.getBody();
            problem.setInstance(URI.create(request.getRequestURI()));
            return problem;
        }
        log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno", "Error interno del servidor",
                "Ocurrió un error inesperado. Si el problema persiste, contacte al equipo de soporte de Eventify", request);
    }

    private ProblemDetail problema(HttpStatus status, String codigo, String titulo, String detalle, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalle);
        problem.setType(URI.create(BASE_TIPOS + codigo));
        problem.setTitle(titulo);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private static int prioridad(FieldError error) {
        int indice = PRIORIDAD_CODIGOS.indexOf(error.getCode());
        return indice < 0 ? PRIORIDAD_CODIGOS.size() : indice;
    }
}
