package com.cuea.rmp.request.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequestRequest(
        @NotBlank(message = "comments must not be blank when rejecting")
        @Size(max = 1000, message = "comments must be at most 1000 characters")
        String comments
) {}
