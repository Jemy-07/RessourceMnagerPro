package com.cuea.rmp.resource.web;

import com.cuea.rmp.resource.application.dto.CreateSkillCommand;
import com.cuea.rmp.resource.application.dto.SkillResult;
import com.cuea.rmp.resource.web.request.CreateSkillRequest;
import com.cuea.rmp.resource.web.response.SkillResponse;
import org.springframework.stereotype.Component;

@Component
public class SkillWebMapper {

    public CreateSkillCommand toCommand(CreateSkillRequest request) {
        return new CreateSkillCommand(request.orgId(), request.name());
    }

    public SkillResponse toResponse(SkillResult result) {
        return new SkillResponse(result.id(), result.orgId(), result.name());
    }
}
