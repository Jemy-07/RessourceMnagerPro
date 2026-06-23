package com.cuea.rmp.user.web.response;

import com.cuea.rmp.user.domain.Role;

import java.util.UUID;

/** HTTP-facing view of a user. Never includes the password hash. */
public record UserResponse(
        UUID id,
        UUID orgId,
        String fullName,
        String email,
        Role role,
        boolean active
) {}
