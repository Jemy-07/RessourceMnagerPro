package com.cuea.rmp.sync.web.request;

import com.cuea.rmp.sync.domain.EntityType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SyncEntryRequest(
        @NotNull(message = "entityType is required")
        EntityType entityType,

        @NotNull(message = "id is required")
        UUID id,

        Map<String, Object> payload,

        @NotNull(message = "clientUpdatedAt is required")
        Instant clientUpdatedAt,

        long clientVersion,

        boolean deleted
) {}
