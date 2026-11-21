package com.example.bth5report.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<Map<String, Object>> error(HttpStatus code, String message) {
        return ResponseEntity.status(code).body(Map.of("timestamp", LocalDateTime.now().toString(),
                "status", code.value(), "message", message));
    }
    @ExceptionHandler(MissingCostException.class)
    ResponseEntity<Map<String,Object>> missingCost(MissingCostException e) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
    @ExceptionHandler(UpstreamException.class)
    ResponseEntity<Map<String,Object>> upstream(UpstreamException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,Object>> badInput(IllegalArgumentException e) {
        return error(HttpStatus.BAD_GATEWAY, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String,Object>> status(ResponseStatusException e) {
        HttpStatus code = HttpStatus.resolve(e.getStatusCode().value());
        return error(code == null ? HttpStatus.INTERNAL_SERVER_ERROR : code,
                e.getReason() == null ? "Request failed" : e.getReason());
    }
}
