package com.cuea.rmp.sync.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyncPushRequest(
        @NotNull(message = "changes is required")
        @Valid
        List<SyncEntryRequest> changes
) {}
