package com.cuea.rmp.reporting.application.usecase;

import com.cuea.rmp.reporting.application.dto.ReportType;
import com.cuea.rmp.reporting.application.port.in.CostReportUseCase;
import com.cuea.rmp.reporting.application.port.in.ExportReportUseCase;
import com.cuea.rmp.reporting.application.port.in.SkillsGapReportUseCase;
import com.cuea.rmp.reporting.application.port.in.UtilizationReportUseCase;
import com.cuea.rmp.reporting.application.port.out.PdfExporter;
import org.springframework.stereotype.Service;

import java.util.List;

/** Renders any report to PDF by flattening its rows into a titled table. */
@Service
public class ExportReportService implements ExportReportUseCase {

    private final UtilizationReportUseCase utilization;
    private final SkillsGapReportUseCase skillsGap;
    private final CostReportUseCase cost;
    private final PdfExporter pdfExporter;

    public ExportReportService(UtilizationReportUseCase utilization,
                               SkillsGapReportUseCase skillsGap,
                               CostReportUseCase cost,
                               PdfExporter pdfExporter) {
        this.utilization = utilization;
        this.skillsGap = skillsGap;
        this.cost = cost;
        this.pdfExporter = pdfExporter;
    }

    @Override
    public byte[] exportPdf(ReportType type) {
        return switch (type) {
            case UTILIZATION -> pdfExporter.exportTable(
                    "Resource Utilization Report",
                    List.of("Resource", "Active Assignments", "Allocated %", "Level"),
                    utilization.generate().stream()
                            .map(r -> List.of(
                                    r.resourceName(),
                                    String.valueOf(r.activeAssignments()),
                                    r.allocatedPct() + "%",
                                    r.level()))
                            .toList());
            case SKILLS_GAP -> pdfExporter.exportTable(
                    "Skills Gap Report",
                    List.of("Skill", "Resources", "Avg Proficiency", "Gap"),
                    skillsGap.generate().stream()
                            .map(r -> List.of(
                                    r.skillName(),
                                    String.valueOf(r.resourceCount()),
                                    String.valueOf(r.avgProficiency()),
                                    r.gap() ? "YES" : "no"))
                            .toList());
            case COST -> pdfExporter.exportTable(
                    "Cost & Margin by Project",
                    List.of("Project", "Currency", "Total", "Allocated", "Spent", "Margin", "Margin %"),
                    cost.generate().stream()
                            .map(r -> List.of(
                                    r.projectName(),
                                    r.currency(),
                                    r.totalAmount().toPlainString(),
                                    r.allocatedAmount().toPlainString(),
                                    r.spentAmount().toPlainString(),
                                    r.margin().toPlainString(),
                                    r.marginPct() + "%"))
                            .toList());
        };
    }
}
