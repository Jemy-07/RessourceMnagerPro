package com.cuea.rmp.shared.domain;

/** Offline-sync state of an aggregate. New records start {@link #PENDING}. */
public enum SyncStatus {
    PENDING,
    SYNCED
}
