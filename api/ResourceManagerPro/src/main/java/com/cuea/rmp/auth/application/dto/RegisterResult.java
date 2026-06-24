package com.cuea.rmp.auth.application.dto;

import com.cuea.rmp.user.domain.Role;

import java.util.UUID;

public record RegisterResult(
        UUID userId,
        String email,
        Role role
) {}
