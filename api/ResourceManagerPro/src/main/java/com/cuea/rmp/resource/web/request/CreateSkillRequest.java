package com.cuea.rmp.resource.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSkillRequest(
        @NotNull(message = "orgId is required")
        UUID orgId,

        @NotBlank(message = "name must not be blank")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name
) {}
