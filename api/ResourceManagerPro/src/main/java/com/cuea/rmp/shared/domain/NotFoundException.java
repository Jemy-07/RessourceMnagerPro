package com.cuea.rmp.shared.domain;

/** A requested aggregate/entity does not exist. Maps to HTTP 404. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }

    public NotFoundException(String message, String code) {
        super(message, code);
    }
}
