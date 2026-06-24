package com.cuea.rmp.resource.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.resource.application.port.in.CreateSkillUseCase;
import com.cuea.rmp.resource.application.port.in.ListSkillsUseCase;
import com.cuea.rmp.resource.web.request.CreateSkillRequest;
import com.cuea.rmp.resource.web.response.SkillResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final CreateSkillUseCase createSkill;
    private final ListSkillsUseCase listSkills;
    private final SkillWebMapper mapper;

    public SkillController(CreateSkillUseCase createSkill, ListSkillsUseCase listSkills, SkillWebMapper mapper) {
        this.createSkill = createSkill;
        this.listSkills = listSkills;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<SkillResponse>> create(@Valid @RequestBody CreateSkillRequest request) {
        SkillResponse body = mapper.toResponse(createSkill.create(mapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Skill created"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SkillResponse>> list() {
        return ApiResponse.ok(listSkills.list().stream().map(mapper::toResponse).toList());
    }
}
