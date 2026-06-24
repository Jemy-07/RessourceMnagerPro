package com.cuea.rmp.resource.application.dto;

import java.util.UUID;

public record CreateSkillCommand(UUID orgId, String name) {}
