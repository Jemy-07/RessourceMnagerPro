package com.cuea.rmp.sync.web.response;

import com.cuea.rmp.sync.application.dto.SyncRow;

import java.time.Instant;
import java.util.List;

public record PullResponse(Instant serverTime, int count, List<SyncRow> changes) {}
