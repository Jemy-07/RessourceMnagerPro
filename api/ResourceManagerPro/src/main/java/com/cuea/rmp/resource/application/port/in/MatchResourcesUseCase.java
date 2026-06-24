package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.ResourceMatchResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MatchResourcesUseCase {
    /** Available resources holding {@code skillId} across [from, to], ordered by proficiency desc. */
    List<ResourceMatchResult> match(UUID skillId, LocalDate from, LocalDate to);
}
