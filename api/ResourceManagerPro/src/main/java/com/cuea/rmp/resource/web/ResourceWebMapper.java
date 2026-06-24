package com.cuea.rmp.resource.web;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.application.dto.AddSkillCommand;
import com.cuea.rmp.resource.application.dto.AvailabilityResult;
import com.cuea.rmp.resource.application.dto.CreateResourceCommand;
import com.cuea.rmp.resource.application.dto.ResourceMatchResult;
import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.dto.UpdateResourceCommand;
import com.cuea.rmp.resource.web.request.AddSkillRequest;
import com.cuea.rmp.resource.web.request.CreateResourceRequest;
import com.cuea.rmp.resource.web.request.UpdateResourceRequest;
import com.cuea.rmp.resource.web.response.AvailabilityResponse;
import com.cuea.rmp.resource.web.response.ResourceMatchResponse;
import com.cuea.rmp.resource.web.response.ResourceResponse;
import com.cuea.rmp.resource.web.response.ResourceSkillResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResourceWebMapper {

    public CreateResourceCommand toCommand(CreateResourceRequest request) {
        return new CreateResourceCommand(
                request.orgId(), request.userId(), request.name(), request.type(),
                request.hourlyRateAmount(), request.currency());
    }

    public UpdateResourceCommand toCommand(UUID id, UpdateResourceRequest request) {
        return new UpdateResourceCommand(
                id, request.name(), request.hourlyRateAmount(), request.currency(), request.availabilityStatus());
    }

    public AddSkillCommand toCommand(UUID resourceId, AddSkillRequest request) {
        return new AddSkillCommand(resourceId, request.skillId(), request.proficiency());
    }

    public ResourceResponse toResponse(ResourceResult result) {
        return new ResourceResponse(
                result.id(),
                result.orgId(),
                result.userId(),
                result.name(),
                result.type(),
                result.hourlyRateAmount(),
                result.currency(),
                result.availabilityStatus(),
                result.skills().stream()
                        .map(s -> new ResourceSkillResponse(s.skillId(), s.proficiency()))
                        .toList());
    }

    public PageResult<ResourceResponse> toResponse(PageResult<ResourceResult> page) {
        return new PageResult<>(
                page.content().stream().map(this::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    public AvailabilityResponse toResponse(AvailabilityResult result) {
        return new AvailabilityResponse(
                result.resourceId(), result.from(), result.to(), result.available(), result.reason());
    }

    public ResourceMatchResponse toResponse(ResourceMatchResult result) {
        return new ResourceMatchResponse(
                result.resourceId(), result.name(), result.type(),
                result.proficiency(), result.hourlyRateAmount(), result.currency());
    }
}
