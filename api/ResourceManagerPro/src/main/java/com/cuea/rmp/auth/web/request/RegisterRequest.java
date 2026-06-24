package com.cuea.rmp.auth.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRequest(
        @NotNull(message = "orgId is required")
        UUID orgId,

        @NotBlank(message = "fullName must not be blank")
        @Size(max = 255, message = "fullName must be at most 255 characters")
        String fullName,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid address")
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 100, message = "password must be 8-100 characters")
        String password
) {}
