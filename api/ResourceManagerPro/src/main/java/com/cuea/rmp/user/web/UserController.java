package com.cuea.rmp.user.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.CreateUserUseCase;
import com.cuea.rmp.user.application.port.in.DeactivateUserUseCase;
import com.cuea.rmp.user.application.port.in.GetUserUseCase;
import com.cuea.rmp.user.application.port.in.ListUsersUseCase;
import com.cuea.rmp.user.application.port.in.UpdateUserUseCase;
import com.cuea.rmp.user.web.request.CreateUserRequest;
import com.cuea.rmp.user.web.request.UpdateUserRequest;
import com.cuea.rmp.user.web.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUser;
    private final GetUserUseCase getUser;
    private final ListUsersUseCase listUsers;
    private final UpdateUserUseCase updateUser;
    private final DeactivateUserUseCase deactivateUser;
    private final UserWebMapper mapper;

    public UserController(CreateUserUseCase createUser,
                          GetUserUseCase getUser,
                          ListUsersUseCase listUsers,
                          UpdateUserUseCase updateUser,
                          DeactivateUserUseCase deactivateUser,
                          UserWebMapper mapper) {
        this.createUser = createUser;
        this.getUser = getUser;
        this.listUsers = listUsers;
        this.updateUser = updateUser;
        this.deactivateUser = deactivateUser;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResult result = createUser.create(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(result), "User created"));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(mapper.toResponse(getUser.get(id)));
    }

    @GetMapping
    public ApiResponse<PageResult<UserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(mapper.toResponse(listUsers.list(page, size)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        UserResult result = updateUser.update(mapper.toCommand(id, request));
        return ApiResponse.ok(mapper.toResponse(result), "User updated");
    }

    @PostMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        deactivateUser.deactivate(id);
        return ApiResponse.ok(null, "User deactivated");
    }
}
