package com.cuea.rmp.user.application.dto;

import com.cuea.rmp.user.domain.Role;

import java.util.UUID;

/** Input to update a user's editable attributes. */
public record UpdateUserCommand(
        UUID id,
        String fullName,
        Role role
) {}
