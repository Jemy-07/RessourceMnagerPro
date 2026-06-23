package com.cuea.rmp.shared.domain;

/** The caller is not authenticated / not permitted for this domain action. Maps to HTTP 401. */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, String code) {
        super(message, code);
    }
}
