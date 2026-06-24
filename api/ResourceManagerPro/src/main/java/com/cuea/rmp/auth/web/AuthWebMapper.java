package com.cuea.rmp.auth.web;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.LoginCommand;
import com.cuea.rmp.auth.application.dto.LogoutCommand;
import com.cuea.rmp.auth.application.dto.RefreshCommand;
import com.cuea.rmp.auth.application.dto.RegisterCommand;
import com.cuea.rmp.auth.application.dto.RegisterResult;
import com.cuea.rmp.auth.web.request.LoginRequest;
import com.cuea.rmp.auth.web.request.LogoutRequest;
import com.cuea.rmp.auth.web.request.RefreshRequest;
import com.cuea.rmp.auth.web.request.RegisterRequest;
import com.cuea.rmp.auth.web.response.AuthResponse;
import com.cuea.rmp.auth.web.response.RegisteredUserResponse;
import org.springframework.stereotype.Component;

/** Translates auth web Request/Response models to/from application Command/Result DTOs. */
@Component
public class AuthWebMapper {

    private static final String TOKEN_TYPE = "Bearer";

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(request.orgId(), request.fullName(), request.email(), request.password());
    }

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    public RefreshCommand toCommand(RefreshRequest request) {
        return new RefreshCommand(request.refreshToken());
    }

    public LogoutCommand toCommand(LogoutRequest request) {
        return new LogoutCommand(request.refreshToken());
    }

    public RegisteredUserResponse toResponse(RegisterResult result) {
        return new RegisteredUserResponse(result.userId(), result.email(), result.role());
    }

    public AuthResponse toResponse(AuthTokens tokens) {
        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                TOKEN_TYPE,
                tokens.accessExpiresInSeconds());
    }
}
