package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.port.in.GetResourceUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetResourceService implements GetResourceUseCase {

    private final ResourceRepository resourceRepository;

    public GetResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ResourceResult get(UUID id) {
        return resourceRepository.findById(id)
                .map(ResourceResult::from)
                .orElseThrow(() -> new NotFoundException("Resource " + id + " not found"));
    }
}
