package com.cuea.rmp.sync.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A single change pushed by a client. Pure data.
 *
 * @param payload          the entity's field values (scalar columns)
 * @param clientUpdatedAt  when the client last edited the row (drives last-write-wins)
 * @param clientVersion    the optimistic-lock version the client based its edit on
 * @param deleted          true for a soft delete
 */
public record SyncEntry(
        EntityType entityType,
        UUID id,
        Map<String, Object> payload,
        Instant clientUpdatedAt,
        long clientVersion,
        boolean deleted
) {}
