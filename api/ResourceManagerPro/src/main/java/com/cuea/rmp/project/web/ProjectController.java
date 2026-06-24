package com.cuea.rmp.project.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.application.port.in.AssignResourceUseCase;
import com.cuea.rmp.project.application.port.in.CreateProjectUseCase;
import com.cuea.rmp.project.application.port.in.DeleteProjectUseCase;
import com.cuea.rmp.project.application.port.in.GetProjectUseCase;
import com.cuea.rmp.project.application.port.in.ListProjectAssignmentsUseCase;
import com.cuea.rmp.project.application.port.in.ListProjectsUseCase;
import com.cuea.rmp.project.application.port.in.UpdateProjectUseCase;
import com.cuea.rmp.project.web.request.AssignResourceRequest;
import com.cuea.rmp.project.web.request.CreateProjectRequest;
import com.cuea.rmp.project.web.request.UpdateProjectRequest;
import com.cuea.rmp.project.web.response.AssignmentResponse;
import com.cuea.rmp.project.web.response.ProjectResponse;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final CreateProjectUseCase createProject;
    private final GetProjectUseCase getProject;
    private final ListProjectsUseCase listProjects;
    private final UpdateProjectUseCase updateProject;
    private final DeleteProjectUseCase deleteProject;
    private final ListProjectAssignmentsUseCase listProjectAssignments;
    private final AssignResourceUseCase assignResource;
    private final ProjectWebMapper projectMapper;
    private final AssignmentWebMapper assignmentMapper;

    public ProjectController(CreateProjectUseCase createProject,
                             GetProjectUseCase getProject,
                             ListProjectsUseCase listProjects,
                             UpdateProjectUseCase updateProject,
                             DeleteProjectUseCase deleteProject,
                             ListProjectAssignmentsUseCase listProjectAssignments,
                             AssignResourceUseCase assignResource,
                             ProjectWebMapper projectMapper,
                             AssignmentWebMapper assignmentMapper) {
        this.createProject = createProject;
        this.getProject = getProject;
        this.listProjects = listProjects;
        this.updateProject = updateProject;
        this.deleteProject = deleteProject;
        this.listProjectAssignments = listProjectAssignments;
        this.assignResource = assignResource;
        this.projectMapper = projectMapper;
        this.assignmentMapper = assignmentMapper;
    }

    // ---- project writes: MANAGER (or ADMIN) ----

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse body = projectMapper.toResponse(createProject.create(projectMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Project created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ProjectResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateProjectRequest request) {
        return ApiResponse.ok(projectMapper.toResponse(updateProject.update(projectMapper.toCommand(id, request))),
                "Project updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        deleteProject.delete(id);
        return ApiResponse.ok(null, "Project deleted");
    }

    // ---- assignments sub-resource ----

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(@PathVariable UUID id,
                                                                  @Valid @RequestBody AssignResourceRequest request) {
        AssignmentResponse body = assignmentMapper.toResponse(
                assignResource.assign(assignmentMapper.toCommand(id, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Resource assigned"));
    }

    // ---- reads: any authenticated user ----

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProjectResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(projectMapper.toResponse(getProject.get(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResult<ProjectResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(projectMapper.toResponse(listProjects.list(page, size)));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AssignmentResponse>> listAssignments(@PathVariable UUID id) {
        return ApiResponse.ok(listProjectAssignments.listByProject(id).stream()
                .map(assignmentMapper::toResponse).toList());
    }
}
