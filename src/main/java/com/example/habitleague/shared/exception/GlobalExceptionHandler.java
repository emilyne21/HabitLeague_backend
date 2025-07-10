package com.example.habitleague.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildBody(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    // 400 Bad Request – parámetros malformados o argumentos inválidos
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        String msg = String.format("Parámetro '%s' con valor '%s' no es válido",
                ex.getName(), ex.getValue());
        return new ResponseEntity<>(
                buildBody(HttpStatus.BAD_REQUEST, "Bad Request", msg, req.getRequestURI()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                validationErrors.putIfAbsent(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> body = buildBody(
                HttpStatus.BAD_REQUEST, "Bad Request", "Errores de validación", req.getRequestURI());
        body.put("validationErrors", validationErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI()),
                HttpStatus.BAD_REQUEST
        );
    }

    // 401 Unauthorized – credenciales inválidas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req.getRequestURI()),
                HttpStatus.UNAUTHORIZED
        );
    }

    // 403 Forbidden – acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.FORBIDDEN, "Forbidden",
                        "No tienes permiso para realizar esta acción",
                        req.getRequestURI()),
                HttpStatus.FORBIDDEN
        );
    }

    // 404 Not Found – entidad o usuario no encontrado
    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(
            RuntimeException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI()),
                HttpStatus.NOT_FOUND
        );
    }

    // 409 Conflict – usuario ya existe
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req.getRequestURI()),
                HttpStatus.CONFLICT
        );
    }

    // 409 Conflict – contenido específico para ChallengeException
    @ExceptionHandler(ChallengeException.class)
    public ResponseEntity<Map<String, Object>> handleChallengeException(ChallengeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", "/api/challenges");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 500 Internal Server Error – fallback genérico
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                        ex.getMessage(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}