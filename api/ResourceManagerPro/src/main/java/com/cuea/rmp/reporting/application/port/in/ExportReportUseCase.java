package com.cuea.rmp.reporting.application.port.in;

import com.cuea.rmp.reporting.application.dto.ReportType;

public interface ExportReportUseCase {
    /** Renders the given report as a PDF document. */
    byte[] exportPdf(ReportType type);
}
