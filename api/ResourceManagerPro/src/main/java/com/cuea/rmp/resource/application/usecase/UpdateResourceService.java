package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.dto.UpdateResourceCommand;
import com.cuea.rmp.resource.application.port.in.UpdateResourceUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.shared.domain.Money;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateResourceService implements UpdateResourceUseCase {

    private final ResourceRepository resourceRepository;

    public UpdateResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ResourceResult update(UpdateResourceCommand command) {
        Resource resource = resourceRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Resource " + command.id() + " not found"));
        resource.rename(command.name());
        resource.changeHourlyRate(Money.of(command.hourlyRateAmount(), command.currency()));
        resource.changeAvailabilityStatus(command.availabilityStatus());
        return ResourceResult.from(resourceRepository.save(resource));
    }
}
