package com.cuea.rmp.shared.domain;

/** A domain invariant / business rule was violated. Maps to HTTP 422. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, String code) {
        super(message, code);
    }
}
