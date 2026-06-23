package com.cuea.rmp.user.application.dto;

import com.cuea.rmp.user.domain.Role;

import java.util.UUID;

/**
 * Input to create a user. {@code password} is the raw secret from the caller;
 * hashing is introduced in M3 (until then it is stored as-is as the hash).
 */
public record CreateUserCommand(
        UUID orgId,
        String fullName,
        String email,
        String password,
        Role role
) {}
