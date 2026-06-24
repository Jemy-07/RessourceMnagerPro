package com.cuea.rmp.auth.application.port.out;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.TokenClaims;

import java.util.UUID;

/**
 * Outbound port for issuing and parsing authentication tokens. Implementations
 * (JWT) keep the token library out of the application layer.
 */
public interface TokenProvider {

    AuthTokens issueTokens(UUID userId, String role, UUID orgId, String email);

    /** Validate signature/expiry, require an access token, and return its claims. */
    TokenClaims parseAccessToken(String token);

    /** Validate signature/expiry, require a refresh token, and return its claims. */
    TokenClaims parseRefreshToken(String token);
}
