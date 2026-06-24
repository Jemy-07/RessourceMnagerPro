package com.cuea.rmp.sync.web.response;

import com.cuea.rmp.sync.application.dto.ConflictInfo;

import java.util.List;

public record PushResponse(int appliedCount, int conflictCount, List<ConflictInfo> conflicts) {}
