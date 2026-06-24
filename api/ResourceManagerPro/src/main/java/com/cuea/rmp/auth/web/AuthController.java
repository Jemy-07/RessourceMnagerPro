package com.cuea.rmp.auth.web;

import com.cuea.rmp.auth.application.port.in.LoginUseCase;
import com.cuea.rmp.auth.application.port.in.LogoutUseCase;
import com.cuea.rmp.auth.application.port.in.RefreshUseCase;
import com.cuea.rmp.auth.application.port.in.RegisterUseCase;
import com.cuea.rmp.auth.web.request.LoginRequest;
import com.cuea.rmp.auth.web.request.LogoutRequest;
import com.cuea.rmp.auth.web.request.RefreshRequest;
import com.cuea.rmp.auth.web.request.RegisterRequest;
import com.cuea.rmp.auth.web.response.AuthResponse;
import com.cuea.rmp.auth.web.response.RegisteredUserResponse;
import com.cuea.rmp.shared.application.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthWebMapper mapper;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          RefreshUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase,
                          AuthWebMapper mapper) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisteredUserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisteredUserResponse body = mapper.toResponse(registerUseCase.register(mapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(body, "Registration successful"));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse body = mapper.toResponse(loginUseCase.login(mapper.toCommand(request)));
        return ApiResponse.ok(body, "Login successful");
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse body = mapper.toResponse(refreshUseCase.refresh(mapper.toCommand(request)));
        return ApiResponse.ok(body, "Token refreshed");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(mapper.toCommand(request));
        return ApiResponse.ok(null, "Logged out");
    }
}
