package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.AddSkillCommand;
import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.port.in.AddSkillToResourceUseCase;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.application.port.out.SkillRepository;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddSkillToResourceService implements AddSkillToResourceUseCase {

    private final ResourceRepository resourceRepository;
    private final SkillRepository skillRepository;

    public AddSkillToResourceService(ResourceRepository resourceRepository, SkillRepository skillRepository) {
        this.resourceRepository = resourceRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public ResourceResult addSkill(AddSkillCommand command) {
        Resource resource = resourceRepository.findById(command.resourceId())
                .orElseThrow(() -> new NotFoundException("Resource " + command.resourceId() + " not found"));
        if (skillRepository.findById(command.skillId()).isEmpty()) {
            throw new NotFoundException("Skill " + command.skillId() + " not found");
        }
        resource.addSkill(command.skillId(), command.proficiency());
        return ResourceResult.from(resourceRepository.save(resource));
    }
}
