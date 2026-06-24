package com.cuea.rmp.sync.domain;

/** Syncable aggregate types. Maps to a SyncableRepository in the registry. */
public enum EntityType {
    USER,
    RESOURCE,
    PROJECT,
    ASSIGNMENT,
    REQUEST,
    TIMESHEET,
    BUDGET
}
