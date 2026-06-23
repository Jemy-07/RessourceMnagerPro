package com.cuea.rmp.shared.domain;

/**
 * Base type for all domain-level errors. Pure Java — no Spring, no web.
 * <p>
 * Each exception carries a stable machine-readable {@code code} (surfaced in the
 * API envelope) alongside a human-readable message. The web layer's
 * GlobalExceptionHandler maps concrete subtypes to HTTP status codes.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String message, String code) {
        super(message);
        this.code = code;
    }

    protected DomainException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
