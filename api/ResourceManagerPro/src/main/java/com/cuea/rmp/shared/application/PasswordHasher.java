package com.cuea.rmp.shared.application;

/**
 * Outbound port for password hashing. Keeps the application layer free of any
 * Spring Security dependency; the BCrypt implementation lives in infrastructure.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
