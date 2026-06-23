package com.cuea.rmp.user.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email value object. Normalised to trimmed lower-case and format-validated on
 * construction. Pure Java — invariant violations raise {@link BusinessRuleException}.
 */
public final class Email {

    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessRuleException("Email must not be blank", "INVALID_EMAIL");
        }
        String normalised = raw.trim().toLowerCase();
        if (!PATTERN.matcher(normalised).matches()) {
            throw new BusinessRuleException("Invalid email format: " + raw, "INVALID_EMAIL");
        }
        return new Email(normalised);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Email email && value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
