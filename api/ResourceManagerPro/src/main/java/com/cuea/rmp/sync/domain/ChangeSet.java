package com.cuea.rmp.sync.domain;

import java.util.List;

/** A batch of client changes pushed in one request. */
public record ChangeSet(List<SyncEntry> entries) {

    public ChangeSet {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
