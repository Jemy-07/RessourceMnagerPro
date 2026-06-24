package com.cuea.rmp.reporting.application.usecase;

import com.cuea.rmp.reporting.application.dto.UtilizationRow;
import com.cuea.rmp.reporting.application.port.in.UtilizationReportUseCase;
import com.cuea.rmp.reporting.application.port.out.ReportQueryPort;
import org.springframework.stereotype.Service;

import java.util.List;

/** Resource-utilisation heat-map data. Reads projections; derives the level band. */
@Service
public class UtilizationReportService implements UtilizationReportUseCase {

    private final ReportQueryPort reportQueryPort;

    public UtilizationReportService(ReportQueryPort reportQueryPort) {
        this.reportQueryPort = reportQueryPort;
    }

    @Override
    public List<UtilizationRow> generate() {
        return reportQueryPort.utilization().stream()
                .map(d -> new UtilizationRow(
                        d.resourceId(), d.resourceName(), d.activeAssignments(),
                        d.allocatedPct(), level(d.allocatedPct())))
                .toList();
    }

    private static String level(long allocatedPct) {
        if (allocatedPct <= 0) {
            return "IDLE";
        }
        if (allocatedPct < 50) {
            return "LIGHT";
        }
        if (allocatedPct < 100) {
            return "MODERATE";
        }
        return allocatedPct == 100 ? "FULL" : "OVER";
    }
}
