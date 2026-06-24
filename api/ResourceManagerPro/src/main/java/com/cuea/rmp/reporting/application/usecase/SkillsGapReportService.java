package com.cuea.rmp.reporting.application.usecase;

import com.cuea.rmp.reporting.application.dto.SkillsGapRow;
import com.cuea.rmp.reporting.application.port.in.SkillsGapReportUseCase;
import com.cuea.rmp.reporting.application.port.out.ReportQueryPort;
import org.springframework.stereotype.Service;

import java.util.List;

/** Skill supply across resources; flags skills no resource currently holds. */
@Service
public class SkillsGapReportService implements SkillsGapReportUseCase {

    private final ReportQueryPort reportQueryPort;

    public SkillsGapReportService(ReportQueryPort reportQueryPort) {
        this.reportQueryPort = reportQueryPort;
    }

    @Override
    public List<SkillsGapRow> generate() {
        return reportQueryPort.skillSupply().stream()
                .map(d -> new SkillsGapRow(
                        d.skillId(), d.skillName(), d.resourceCount(),
                        round1(d.avgProficiency()), d.resourceCount() == 0))
                .toList();
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
