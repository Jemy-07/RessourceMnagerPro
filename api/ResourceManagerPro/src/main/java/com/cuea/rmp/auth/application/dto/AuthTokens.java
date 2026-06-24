package com.cuea.rmp.auth.application.dto;

import java.time.Instant;

/**
 * Result of a successful authentication. The {@code refreshTokenId} (jti) and
 * {@code refreshExpiresAt} are used by the use case to persist the refresh token
 * in the {@code RefreshTokenStore}; they are not exposed over HTTP.
 */
public record AuthTokens(
        String accessToken,
        String refreshToken,
        long accessExpiresInSeconds,
        String refreshTokenId,
        Instant refreshExpiresAt
) {}
