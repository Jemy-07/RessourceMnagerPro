package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.CreateResourceCommand;
import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.port.in.CreateResourceUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateResourceService implements CreateResourceUseCase {

    private final ResourceRepository resourceRepository;

    public CreateResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ResourceResult create(CreateResourceCommand command) {
        Money hourlyRate = Money.of(command.hourlyRateAmount(), command.currency());
        Resource resource = Resource.create(
                command.orgId(), command.userId(), command.name(), command.type(), hourlyRate);
        return ResourceResult.from(resourceRepository.save(resource));
    }
}
