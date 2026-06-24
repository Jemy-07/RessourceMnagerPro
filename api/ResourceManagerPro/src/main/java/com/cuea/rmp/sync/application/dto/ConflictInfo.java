package com.cuea.rmp.sync.application.dto;

import com.cuea.rmp.sync.domain.EntityType;

import java.util.UUID;

/** Describes a concurrent-edit conflict and how last-write-wins resolved it. */
public record ConflictInfo(
        EntityType entityType,
        UUID id,
        String resolution,   // CLIENT_WON | SERVER_WON
        String message
) {}
