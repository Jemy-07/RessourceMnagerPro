package com.cuea.rmp.auth.application.dto;

import java.util.UUID;

/** Self-service registration input. The resulting account is always a MEMBER. */
public record RegisterCommand(
        UUID orgId,
        String fullName,
        String email,
        String password
) {}
