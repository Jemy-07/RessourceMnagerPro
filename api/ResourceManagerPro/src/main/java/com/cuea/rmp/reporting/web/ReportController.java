package com.cuea.rmp.reporting.web;

import com.cuea.rmp.reporting.application.dto.CostRow;
import com.cuea.rmp.reporting.application.dto.ReportType;
import com.cuea.rmp.reporting.application.dto.SkillsGapRow;
import com.cuea.rmp.reporting.application.dto.UtilizationRow;
import com.cuea.rmp.reporting.application.port.in.CostReportUseCase;
import com.cuea.rmp.reporting.application.port.in.ExportReportUseCase;
import com.cuea.rmp.reporting.application.port.in.SkillsGapReportUseCase;
import com.cuea.rmp.reporting.application.port.in.UtilizationReportUseCase;
import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.shared.domain.BusinessRuleException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporting & export endpoints. MANAGER/ADMIN only.
 * <p>
 * JSON reports use the standard {@code ApiResponse} envelope; {@code /export}
 * streams a raw {@code application/pdf} attachment (binary, no envelope).
 */
@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReportController {

    private final UtilizationReportUseCase utilization;
    private final SkillsGapReportUseCase skillsGap;
    private final CostReportUseCase cost;
    private final ExportReportUseCase exportReport;

    public ReportController(UtilizationReportUseCase utilization,
                            SkillsGapReportUseCase skillsGap,
                            CostReportUseCase cost,
                            ExportReportUseCase exportReport) {
        this.utilization = utilization;
        this.skillsGap = skillsGap;
        this.cost = cost;
        this.exportReport = exportReport;
    }

    @GetMapping("/utilization")
    public ApiResponse<List<UtilizationRow>> utilization() {
        return ApiResponse.ok(utilization.generate());
    }

    @GetMapping("/skills-gap")
    public ApiResponse<List<SkillsGapRow>> skillsGap() {
        return ApiResponse.ok(skillsGap.generate());
    }

    @GetMapping("/cost")
    public ApiResponse<List<CostRow>> cost() {
        return ApiResponse.ok(cost.generate());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam String type,
                                         @RequestParam(defaultValue = "pdf") String format) {
        if (!"pdf".equalsIgnoreCase(format)) {
            throw new BusinessRuleException("Unsupported format: " + format + " (only 'pdf')", "UNSUPPORTED_FORMAT");
        }
        ReportType reportType = parseType(type);
        byte[] pdf = exportReport.exportPdf(reportType);

        String filename = type.toLowerCase() + "-report-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    private ReportType parseType(String type) {
        return switch (type.toLowerCase()) {
            case "utilization" -> ReportType.UTILIZATION;
            case "skills-gap" -> ReportType.SKILLS_GAP;
            case "cost" -> ReportType.COST;
            default -> throw new BusinessRuleException(
                    "Unknown report type: " + type + " (utilization|skills-gap|cost)", "INVALID_REPORT_TYPE");
        };
    }
}
