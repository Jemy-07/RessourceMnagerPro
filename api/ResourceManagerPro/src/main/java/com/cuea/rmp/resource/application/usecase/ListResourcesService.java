package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.port.in.ListResourcesUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.domain.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListResourcesService implements ListResourcesUseCase {

    private final ResourceRepository resourceRepository;

    public ListResourcesService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public PageResult<ResourceResult> list(int page, int size) {
        PageResult<Resource> resources = resourceRepository.findAll(page, size);
        return new PageResult<>(
                resources.content().stream().map(ResourceResult::from).toList(),
                resources.page(),
                resources.size(),
                resources.totalElements(),
                resources.totalPages());
    }
}
