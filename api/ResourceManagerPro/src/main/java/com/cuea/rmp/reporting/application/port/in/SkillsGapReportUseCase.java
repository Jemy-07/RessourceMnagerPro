package com.cuea.rmp.reporting.application.port.in;

import com.cuea.rmp.reporting.application.dto.SkillsGapRow;

import java.util.List;

public interface SkillsGapReportUseCase {
    List<SkillsGapRow> generate();
}
