package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.CreateSkillCommand;
import com.cuea.rmp.resource.application.dto.SkillResult;

public interface CreateSkillUseCase {
    SkillResult create(CreateSkillCommand command);
}
