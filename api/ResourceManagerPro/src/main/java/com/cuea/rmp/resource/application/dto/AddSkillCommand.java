package com.cuea.rmp.resource.application.dto;

import java.util.UUID;

public record AddSkillCommand(UUID resourceId, UUID skillId, int proficiency) {}
