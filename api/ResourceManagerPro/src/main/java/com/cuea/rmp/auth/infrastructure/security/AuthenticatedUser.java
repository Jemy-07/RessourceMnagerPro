package com.cuea.rmp.auth.infrastructure.security;

import java.util.UUID;

/**
 * Principal placed in the SecurityContext after a JWT is validated. Exposes the
 * authenticated user's identity, role, and organisation for {@code CurrentUserProvider}.
 */
public record AuthenticatedUser(
        UUID userId,
        String role,
        UUID orgId,
        String email
) {}
