package com.cuea.rmp.resource.application.usecase;

import com.cuea.rmp.resource.application.dto.SkillResult;
import com.cuea.rmp.resource.application.port.in.ListSkillsUseCase;
import com.cuea.rmp.resource.application.port.out.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListSkillsService implements ListSkillsUseCase {

    private final SkillRepository skillRepository;

    public ListSkillsService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public List<SkillResult> list() {
        return skillRepository.findAll().stream().map(SkillResult::from).toList();
    }
}
