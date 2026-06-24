package com.cuea.rmp.request.web;

import com.cuea.rmp.auth.infrastructure.security.CurrentUserProvider;
import com.cuea.rmp.request.application.dto.ApproveRequestCommand;
import com.cuea.rmp.request.application.dto.RejectRequestCommand;
import com.cuea.rmp.request.application.port.in.ApproveRequestUseCase;
import com.cuea.rmp.request.application.port.in.CreateRequestUseCase;
import com.cuea.rmp.request.application.port.in.ListRequestsUseCase;
import com.cuea.rmp.request.application.port.in.RejectRequestUseCase;
import com.cuea.rmp.request.domain.RequestStatus;
import com.cuea.rmp.request.web.request.CreateRequestRequest;
import com.cuea.rmp.request.web.request.RejectRequestRequest;
import com.cuea.rmp.request.web.response.RequestResponse;
import com.cuea.rmp.shared.application.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final CreateRequestUseCase createRequest;
    private final ListRequestsUseCase listRequests;
    private final ApproveRequestUseCase approveRequest;
    private final RejectRequestUseCase rejectRequest;
    private final RequestWebMapper mapper;
    private final CurrentUserProvider currentUser;

    public RequestController(CreateRequestUseCase createRequest,
                            ListRequestsUseCase listRequests,
                            ApproveRequestUseCase approveRequest,
                            RejectRequestUseCase rejectRequest,
                            RequestWebMapper mapper,
                            CurrentUserProvider currentUser) {
        this.createRequest = createRequest;
        this.listRequests = listRequests;
        this.approveRequest = approveRequest;
        this.rejectRequest = rejectRequest;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    // ---- raise: any authenticated user (MEMBER and up) ----

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RequestResponse>> create(@Valid @RequestBody CreateRequestRequest request) {
        UUID requesterId = currentUser.currentUserId();
        RequestResponse body = mapper.toResponse(createRequest.create(mapper.toCommand(requesterId, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Request created"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RequestResponse>> list(@RequestParam(required = false) RequestStatus status) {
        return ApiResponse.ok(listRequests.list(status).stream().map(mapper::toResponse).toList());
    }

    // ---- approve / reject: APPROVER (or ADMIN) ----

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ApiResponse<RequestResponse> approve(@PathVariable UUID id) {
        UUID approverId = currentUser.currentUserId();
        RequestResponse body = mapper.toResponse(approveRequest.approve(new ApproveRequestCommand(id, approverId)));
        return ApiResponse.ok(body, "Request approved");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ApiResponse<RequestResponse> reject(@PathVariable UUID id,
                                               @Valid @RequestBody RejectRequestRequest request) {
        UUID approverId = currentUser.currentUserId();
        RequestResponse body = mapper.toResponse(
                rejectRequest.reject(new RejectRequestCommand(id, approverId, request.comments())));
        return ApiResponse.ok(body, "Request rejected");
    }
}
