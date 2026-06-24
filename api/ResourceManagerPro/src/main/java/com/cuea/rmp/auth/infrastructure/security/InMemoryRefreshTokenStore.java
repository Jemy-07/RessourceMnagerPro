package com.cuea.rmp.auth.infrastructure.security;

import com.cuea.rmp.auth.application.port.out.RefreshTokenStore;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory refresh-token store keyed by user → tokenId → expiry.
 * <p>
 * NOTE: not persistent and not shared across instances — tokens are dropped on
 * restart. Swap for a Redis/DB-backed adapter before horizontal scaling.
 */
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<UUID, Map<String, Instant>> tokensByUser = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRefreshTokenStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void store(UUID userId, String tokenId, Instant expiresAt) {
        tokensByUser.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(tokenId, expiresAt);
    }

    @Override
    public boolean isValid(UUID userId, String tokenId) {
        Map<String, Instant> tokens = tokensByUser.get(userId);
        if (tokens == null) {
            return false;
        }
        Instant expiresAt = tokens.get(tokenId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now(clock))) {
            tokens.remove(tokenId);
            return false;
        }
        return true;
    }

    @Override
    public void revoke(UUID userId, String tokenId) {
        Map<String, Instant> tokens = tokensByUser.get(userId);
        if (tokens != null) {
            tokens.remove(tokenId);
        }
    }

    @Override
    public void revokeAll(UUID userId) {
        tokensByUser.remove(userId);
    }
}
