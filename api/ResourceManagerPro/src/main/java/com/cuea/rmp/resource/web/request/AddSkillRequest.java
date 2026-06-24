package com.cuea.rmp.resource.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddSkillRequest(
        @NotNull(message = "skillId is required")
        UUID skillId,

        @Min(value = 1, message = "proficiency must be between 1 and 5")
        @Max(value = 5, message = "proficiency must be between 1 and 5")
        int proficiency
) {}
