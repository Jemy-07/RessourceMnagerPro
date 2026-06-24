package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.ResourceMatchResult;
import com.cuea.rmp.resource.application.port.in.MatchResourcesUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.application.port.out.TimeOffRepository;
import com.cuea.rmp.resource.domain.AvailabilityChecker;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.resource.domain.ResourceSkill;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Finds resources holding a skill that are also free across [from, to], ordered
 * by descending proficiency for that skill.
 */
@Service
@Transactional(readOnly = true)
public class MatchResourcesService implements MatchResourcesUseCase {

    private final ResourceRepository resourceRepository;
    private final TimeOffRepository timeOffRepository;
    private final AvailabilityChecker availabilityChecker = new AvailabilityChecker();

    public MatchResourcesService(ResourceRepository resourceRepository, TimeOffRepository timeOffRepository) {
        this.resourceRepository = resourceRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Override
    public List<ResourceMatchResult> match(UUID skillId, LocalDate from, LocalDate to) {
        DateRange window = DateRange.of(from, to);
        List<ResourceMatchResult> candidates = new ArrayList<>();

        for (Resource resource : resourceRepository.findBySkill(skillId)) {
            // NOTE: matching screens on status + time-off; allocation-level booking
            // conflicts are enforced at assignment time by ConflictDetector (M5).
            boolean available = availabilityChecker
                    .check(resource, window, List.of(), timeOffRepository.findByResourceId(resource.getId()))
                    .available();
            if (!available) {
                continue;
            }
            ResourceSkill match = resource.findSkill(skillId).orElseThrow();
            candidates.add(new ResourceMatchResult(
                    resource.getId(),
                    resource.getName(),
                    resource.getType(),
                    match.getProficiency(),
                    resource.getHourlyRate().getAmount(),
                    resource.getHourlyRate().getCurrency()));
        }

        candidates.sort(Comparator.comparingInt(ResourceMatchResult::proficiency).reversed());
        return candidates;
    }
}
