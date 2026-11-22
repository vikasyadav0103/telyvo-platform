package com.telyvo.platform.common;

import com.telyvo.platform.discovery.DiscoveryException;
import com.telyvo.platform.research.ResearchException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(String code, String message, Instant timestamp) {}

    @ExceptionHandler(DiscoveryException.class)
    ResponseEntity<ApiError> handleDiscovery(DiscoveryException ex) {
        return ResponseEntity.status(ex.status())
                .body(new ApiError(ex.code(), ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ResearchException.class)
    ResponseEntity<ApiError> handleResearch(ResearchException ex) {
        return ResponseEntity.status(ex.status())
                .body(new ApiError(ex.code(), ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiError> handleValidation(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("INVALID_REQUEST", "Request validation failed. Check required fields and length limits.", Instant.now()));
    }
}
