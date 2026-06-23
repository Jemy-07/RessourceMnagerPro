package com.cuea.rmp.shared.infrastructure.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.shared.domain.DomainException;
import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.shared.domain.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single point that maps domain & validation errors to HTTP status codes and the
 * uniform {@link ApiResponse} envelope. All endpoints rely on this advice rather
 * than handling errors themselves.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex);
    }

    /** Fallback for any other domain exception subtype. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        return build(HttpStatus.BAD_REQUEST, ex);
    }

    /** Bean-validation failures on {@code @Valid} web Request models. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(),
                    fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage());
        }
        ApiResponse<Map<String, String>> body = new ApiResponse<>(
                false, "Validation failed", fieldErrors, "VALIDATION_ERROR", java.time.Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, DomainException ex) {
        // Domain errors are expected control flow; log at debug, not error.
        log.debug("Domain exception [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getMessage(), ex.getCode()));
    }
}
