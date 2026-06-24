package com.cuea.rmp.sync.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.sync.application.dto.PullResult;
import com.cuea.rmp.sync.application.dto.PushResult;
import com.cuea.rmp.sync.application.port.in.PullChangesUseCase;
import com.cuea.rmp.sync.application.port.in.PushChangesUseCase;
import com.cuea.rmp.sync.domain.ChangeSet;
import com.cuea.rmp.sync.domain.SyncEntry;
import com.cuea.rmp.sync.web.request.SyncPushRequest;
import com.cuea.rmp.sync.web.response.PullResponse;
import com.cuea.rmp.sync.web.response.PushResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@PreAuthorize("isAuthenticated()")
public class SyncController {

    private final PushChangesUseCase pushChanges;
    private final PullChangesUseCase pullChanges;

    public SyncController(PushChangesUseCase pushChanges, PullChangesUseCase pullChanges) {
        this.pushChanges = pushChanges;
        this.pullChanges = pullChanges;
    }

    @PostMapping("/push")
    public ApiResponse<PushResponse> push(@Valid @RequestBody SyncPushRequest request) {
        List<SyncEntry> entries = request.changes().stream()
                .map(e -> new SyncEntry(e.entityType(), e.id(), e.payload(),
                        e.clientUpdatedAt(), e.clientVersion(), e.deleted()))
                .toList();
        PushResult result = pushChanges.push(new ChangeSet(entries));
        return ApiResponse.ok(
                new PushResponse(result.appliedCount(), result.conflictCount(), result.conflicts()),
                "Push processed");
    }

    @GetMapping("/pull")
    public ApiResponse<PullResponse> pull(@RequestParam(required = false) String since) {
        Instant from = parseSince(since);
        PullResult result = pullChanges.pull(from);
        return ApiResponse.ok(new PullResponse(result.serverTime(), result.changes().size(), result.changes()));
    }

    private Instant parseSince(String since) {
        if (since == null || since.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(since);
        } catch (DateTimeParseException ex) {
            throw new BusinessRuleException("Invalid 'since' timestamp; expected ISO-8601 (e.g. 2026-06-24T08:00:00Z)",
                    "INVALID_SINCE");
        }
    }
}
