package com.cuea.rmp.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port tracking live refresh tokens so they can be validated on refresh
 * and revoked on logout (token rotation).
 */
public interface RefreshTokenStore {

    void store(UUID userId, String tokenId, Instant expiresAt);

    boolean isValid(UUID userId, String tokenId);

    void revoke(UUID userId, String tokenId);

    void revokeAll(UUID userId);
}
