package com.cuea.rmp.user.web.request;

import com.cuea.rmp.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "fullName must not be blank")
        @Size(max = 255, message = "fullName must be at most 255 characters")
        String fullName,

        @NotNull(message = "role is required")
        Role role
) {}
