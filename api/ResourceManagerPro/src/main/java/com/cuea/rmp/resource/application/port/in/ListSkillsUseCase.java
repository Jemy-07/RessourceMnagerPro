package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.SkillResult;

import java.util.List;

public interface ListSkillsUseCase {
    List<SkillResult> list();
}
