package com.cuea.rmp.reporting.application.usecase;

import com.cuea.rmp.reporting.application.dto.CostRow;
import com.cuea.rmp.reporting.application.port.in.CostReportUseCase;
import com.cuea.rmp.reporting.application.port.out.ReportQueryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Cost & margin by project, derived from each project's budget. */
@Service
public class CostReportService implements CostReportUseCase {

    private final ReportQueryPort reportQueryPort;

    public CostReportService(ReportQueryPort reportQueryPort) {
        this.reportQueryPort = reportQueryPort;
    }

    @Override
    public List<CostRow> generate() {
        return reportQueryPort.projectCosts().stream()
                .map(d -> {
                    BigDecimal margin = d.totalAmount().subtract(d.spentAmount());
                    double marginPct = d.totalAmount().signum() == 0 ? 0.0
                            : margin.divide(d.totalAmount(), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue();
                    return new CostRow(d.projectId(), d.projectName(), d.currency(),
                            d.totalAmount(), d.allocatedAmount(), d.spentAmount(),
                            margin, Math.round(marginPct * 10.0) / 10.0);
                })
                .toList();
    }
}
