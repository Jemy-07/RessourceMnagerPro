package com.cuea.rmp.sync.application.dto;

import com.cuea.rmp.sync.domain.EntityType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** A persisted row as seen by the sync engine (server-side metadata + payload). */
public record SyncRow(
        EntityType entityType,
        UUID id,
        Map<String, Object> payload,
        Instant updatedAt,
        long version,
        boolean deleted
) {}
