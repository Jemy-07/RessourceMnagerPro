package com.cuea.rmp.auth.web.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "refreshToken must not be blank")
        String refreshToken
) {}
