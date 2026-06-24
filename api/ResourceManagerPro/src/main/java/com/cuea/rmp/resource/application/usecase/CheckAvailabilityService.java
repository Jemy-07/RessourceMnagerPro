package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.AvailabilityResult;
import com.cuea.rmp.resource.application.port.in.CheckAvailabilityUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.application.port.out.TimeOffRepository;
import com.cuea.rmp.resource.domain.AvailabilityChecker;
import com.cuea.rmp.resource.domain.AvailabilityVerdict;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.resource.domain.TimeOff;
import com.cuea.rmp.shared.domain.DateRange;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CheckAvailabilityService implements CheckAvailabilityUseCase {

    private final ResourceRepository resourceRepository;
    private final TimeOffRepository timeOffRepository;
    private final AvailabilityChecker availabilityChecker = new AvailabilityChecker();

    public CheckAvailabilityService(ResourceRepository resourceRepository, TimeOffRepository timeOffRepository) {
        this.resourceRepository = resourceRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Override
    public AvailabilityResult check(UUID resourceId, LocalDate from, LocalDate to) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource " + resourceId + " not found"));
        DateRange window = DateRange.of(from, to);
        List<TimeOff> timeOffs = timeOffRepository.findByResourceId(resourceId);
        // NOTE: binary availability reflects status + approved time-off only. Assignment
        // conflicts are allocation-based and enforced at booking time by ConflictDetector (M5).
        AvailabilityVerdict verdict = availabilityChecker.check(resource, window, List.of(), timeOffs);
        return AvailabilityResult.of(resourceId, from, to, verdict);
    }
}
