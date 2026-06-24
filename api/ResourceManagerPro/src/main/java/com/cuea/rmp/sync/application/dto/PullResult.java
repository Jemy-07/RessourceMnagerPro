package com.cuea.rmp.sync.application.dto;

import java.time.Instant;
import java.util.List;

/** {@code serverTime} should be sent back as {@code since} on the next pull. */
public record PullResult(Instant serverTime, List<SyncRow> changes) {}
