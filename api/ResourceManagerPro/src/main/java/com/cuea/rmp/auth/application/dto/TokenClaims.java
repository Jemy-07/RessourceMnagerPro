package com.cuea.rmp.auth.application.dto;

import java.util.UUID;

/** Framework-agnostic view of the claims carried by a JWT. */
public record TokenClaims(
        UUID userId,
        String role,
        UUID orgId,
        String email,
        String tokenId
) {}
