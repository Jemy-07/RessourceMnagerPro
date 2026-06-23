package com.cuea.rmp.user.application.dto;

import com.cuea.rmp.user.domain.Role;
import com.cuea.rmp.user.domain.User;

import java.util.UUID;

/** Output DTO describing a user. Never carries the password hash. */
public record UserResult(
        UUID id,
        UUID orgId,
        String fullName,
        String email,
        Role role,
        boolean active
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getId(),
                user.getOrgId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getRole(),
                user.isActive());
    }
}
