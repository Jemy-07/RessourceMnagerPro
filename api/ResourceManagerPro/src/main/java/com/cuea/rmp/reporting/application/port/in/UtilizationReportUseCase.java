package com.cuea.rmp.reporting.application.port.in;

import com.cuea.rmp.reporting.application.dto.UtilizationRow;

import java.util.List;

public interface UtilizationReportUseCase {
    List<UtilizationRow> generate();
}
