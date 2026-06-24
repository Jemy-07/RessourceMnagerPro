package com.cuea.rmp.sync.application.port.out;

import com.cuea.rmp.sync.application.dto.SyncRow;
import com.cuea.rmp.sync.domain.EntityType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic persistence port the sync engine drives, one implementation per
 * {@link EntityType}. Operates on scalar payloads + base metadata.
 */
public interface SyncableRepository {

    EntityType entityType();

    Optional<SyncRow> findById(UUID id);

    /** Rows changed after {@code since} — INCLUDING soft-deleted ones. */
    List<SyncRow> findUpdatedSince(Instant since);

    /** Insert/update from a payload (soft-delete when {@code deleted}); marks the row SYNCED. */
    SyncRow upsert(UUID id, Map<String, Object> payload, boolean deleted);

    /** Mark an existing row SYNCED without changing its data (server-wins case). */
    void markSynced(UUID id);
}
