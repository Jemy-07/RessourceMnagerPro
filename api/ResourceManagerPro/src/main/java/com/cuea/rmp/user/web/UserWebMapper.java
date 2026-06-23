package com.cuea.rmp.user.web;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.dto.UpdateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.web.request.CreateUserRequest;
import com.cuea.rmp.user.web.request.UpdateUserRequest;
import com.cuea.rmp.user.web.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Translates between web Request/Response models and application Command/Result DTOs. */
@Component
public class UserWebMapper {

    public CreateUserCommand toCommand(CreateUserRequest request) {
        return new CreateUserCommand(
                request.orgId(),
                request.fullName(),
                request.email(),
                request.password(),
                request.role());
    }

    public UpdateUserCommand toCommand(UUID id, UpdateUserRequest request) {
        return new UpdateUserCommand(id, request.fullName(), request.role());
    }

    public UserResponse toResponse(UserResult result) {
        return new UserResponse(
                result.id(),
                result.orgId(),
                result.fullName(),
                result.email(),
                result.role(),
                result.active());
    }

    public PageResult<UserResponse> toResponse(PageResult<UserResult> page) {
        return new PageResult<>(
                page.content().stream().map(this::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}
