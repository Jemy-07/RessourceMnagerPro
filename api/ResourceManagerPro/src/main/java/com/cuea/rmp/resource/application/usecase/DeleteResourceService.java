package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.port.in.DeleteResourceUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteResourceService implements DeleteResourceUseCase {

    private final ResourceRepository resourceRepository;

    public DeleteResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void delete(UUID id) {
        if (resourceRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Resource " + id + " not found");
        }
        resourceRepository.softDelete(id);
    }
}
