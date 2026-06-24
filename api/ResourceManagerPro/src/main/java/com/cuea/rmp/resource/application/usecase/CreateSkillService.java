package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.CreateSkillCommand;
import com.cuea.rmp.resource.application.dto.SkillResult;
import com.cuea.rmp.resource.application.port.in.CreateSkillUseCase;
import com.cuea.rmp.resource.application.port.out.SkillRepository;
import com.cuea.rmp.resource.domain.Skill;
import com.cuea.rmp.shared.domain.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateSkillService implements CreateSkillUseCase {

    private final SkillRepository skillRepository;

    public CreateSkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillResult create(CreateSkillCommand command) {
        if (command.name() != null && skillRepository.existsByOrgIdAndName(command.orgId(), command.name().trim())) {
            throw new ConflictException("Skill '" + command.name() + "' already exists", "SKILL_ALREADY_EXISTS");
        }
        Skill skill = Skill.create(command.orgId(), command.name());
        return SkillResult.from(skillRepository.save(skill));
    }
}
