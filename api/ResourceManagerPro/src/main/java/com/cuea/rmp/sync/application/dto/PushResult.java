package com.cuea.rmp.sync.application.dto;

import java.util.List;

public record PushResult(int appliedCount, int conflictCount, List<ConflictInfo> conflicts) {}
