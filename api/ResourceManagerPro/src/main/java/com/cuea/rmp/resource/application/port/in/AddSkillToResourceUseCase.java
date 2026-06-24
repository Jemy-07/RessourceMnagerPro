package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.AddSkillCommand;
import com.cuea.rmp.resource.application.dto.ResourceResult;

public interface AddSkillToResourceUseCase {
    ResourceResult addSkill(AddSkillCommand command);
}
