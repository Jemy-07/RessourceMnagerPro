package com.cuea.rmp.reporting.application.port.in;

import com.cuea.rmp.reporting.application.dto.CostRow;

import java.util.List;

public interface CostReportUseCase {
    List<CostRow> generate();
}
