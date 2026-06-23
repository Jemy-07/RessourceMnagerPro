package com.cuea.rmp.shared.domain;

/** The request conflicts with current state (e.g. duplicate). Maps to HTTP 409. */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message, "CONFLICT");
    }

    public ConflictException(String message, String code) {
        super(message, code);
    }
}
