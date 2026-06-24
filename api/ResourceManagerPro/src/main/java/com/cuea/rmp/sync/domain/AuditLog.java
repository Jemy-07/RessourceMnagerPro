package com.cuea.rmp.sync.domain;

import com.cuea.rmp.shared.domain.AuditAction;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Append-only audit record. The sync engine writes one (flagged {@code conflict})
 * for the losing side of a concurrent edit resolved by last-write-wins.
 */
public class AuditLog {

    private final UUID id;
    private final String entityType;
    private final UUID entityId;
    private final AuditAction action;
    private final boolean conflict;
    private final String message;
    private final Instant occurredAt;

    private AuditLog(UUID id, String entityType, UUID entityId, AuditAction action,
                     boolean conflict, String message, Instant occurredAt) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.conflict = conflict;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public static AuditLog conflict(String entityType, UUID entityId, AuditAction action,
                                    String message, Instant occurredAt) {
        return new AuditLog(UUID.randomUUID(), entityType, entityId, action, true, message, occurredAt);
    }

    public static AuditLog of(String entityType, UUID entityId, AuditAction action,
                              boolean conflict, String message, Instant occurredAt) {
        return new AuditLog(UUID.randomUUID(), entityType, entityId, action, conflict, message, occurredAt);
    }

    public static AuditLog reconstitute(UUID id, String entityType, UUID entityId, AuditAction action,
                                        boolean conflict, String message, Instant occurredAt) {
        return new AuditLog(Objects.requireNonNull(id), entityType, entityId, action, conflict, message, occurredAt);
    }

    public UUID getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public boolean isConflict() {
        return conflict;
    }

    public String getMessage() {
        return message;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
