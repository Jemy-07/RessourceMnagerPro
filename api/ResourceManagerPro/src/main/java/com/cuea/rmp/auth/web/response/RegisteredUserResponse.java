package com.cuea.rmp.auth.web.response;

import com.cuea.rmp.user.domain.Role;

import java.util.UUID;

public record RegisteredUserResponse(
        UUID userId,
        String email,
        Role role
) {}
