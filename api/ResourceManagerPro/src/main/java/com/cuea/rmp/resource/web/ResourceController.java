package com.cuea.rmp.resource.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.application.port.in.AddSkillToResourceUseCase;
import com.cuea.rmp.resource.application.port.in.CheckAvailabilityUseCase;
import com.cuea.rmp.resource.application.port.in.CreateResourceUseCase;
import com.cuea.rmp.resource.application.port.in.DeleteResourceUseCase;
import com.cuea.rmp.resource.application.port.in.GetResourceUseCase;
import com.cuea.rmp.resource.application.port.in.ListResourcesUseCase;
import com.cuea.rmp.resource.application.port.in.MatchResourcesUseCase;
import com.cuea.rmp.resource.application.port.in.UpdateResourceUseCase;
import com.cuea.rmp.resource.web.request.AddSkillRequest;
import com.cuea.rmp.resource.web.request.CreateResourceRequest;
import com.cuea.rmp.resource.web.request.UpdateResourceRequest;
import com.cuea.rmp.resource.web.response.AvailabilityResponse;
import com.cuea.rmp.resource.web.response.ResourceMatchResponse;
import com.cuea.rmp.resource.web.response.ResourceResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final CreateResourceUseCase createResource;
    private final GetResourceUseCase getResource;
    private final ListResourcesUseCase listResources;
    private final UpdateResourceUseCase updateResource;
    private final DeleteResourceUseCase deleteResource;
    private final AddSkillToResourceUseCase addSkillToResource;
    private final CheckAvailabilityUseCase checkAvailability;
    private final MatchResourcesUseCase matchResources;
    private final ResourceWebMapper mapper;

    public ResourceController(CreateResourceUseCase createResource,
                              GetResourceUseCase getResource,
                              ListResourcesUseCase listResources,
                              UpdateResourceUseCase updateResource,
                              DeleteResourceUseCase deleteResource,
                              AddSkillToResourceUseCase addSkillToResource,
                              CheckAvailabilityUseCase checkAvailability,
                              MatchResourcesUseCase matchResources,
                              ResourceWebMapper mapper) {
        this.createResource = createResource;
        this.getResource = getResource;
        this.listResources = listResources;
        this.updateResource = updateResource;
        this.deleteResource = deleteResource;
        this.addSkillToResource = addSkillToResource;
        this.checkAvailability = checkAvailability;
        this.matchResources = matchResources;
        this.mapper = mapper;
    }

    // ---- writes: MANAGER (or ADMIN) ----

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ResourceResponse>> create(@Valid @RequestBody CreateResourceRequest request) {
        ResourceResponse body = mapper.toResponse(createResource.create(mapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Resource created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ResourceResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateResourceRequest request) {
        return ApiResponse.ok(mapper.toResponse(updateResource.update(mapper.toCommand(id, request))), "Resource updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        deleteResource.delete(id);
        return ApiResponse.ok(null, "Resource deleted");
    }

    @PostMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ResourceResponse> addSkill(@PathVariable UUID id,
                                                  @Valid @RequestBody AddSkillRequest request) {
        return ApiResponse.ok(mapper.toResponse(addSkillToResource.addSkill(mapper.toCommand(id, request))), "Skill added");
    }

    // ---- reads: any authenticated user (MEMBER and up) ----

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ResourceResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(mapper.toResponse(getResource.get(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResult<ResourceResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(mapper.toResponse(listResources.list(page, size)));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AvailabilityResponse> availability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(mapper.toResponse(checkAvailability.check(id, from, to)));
    }

    @GetMapping("/match")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ResourceMatchResponse>> match(
            @RequestParam UUID skillId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(matchResources.match(skillId, from, to).stream().map(mapper::toResponse).toList());
    }
}
