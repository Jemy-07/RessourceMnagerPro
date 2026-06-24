package com.cuea.rmp.auth.web.response;

/** Token bundle returned by login/refresh. */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}
