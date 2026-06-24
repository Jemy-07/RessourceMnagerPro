package com.cuea.rmp.reporting.infrastructure.persistence;

import java.math.BigDecimal;

public interface ProjectCostProjection {
    String getProjectId();
    String getProjectName();
    String getCurrency();
    BigDecimal getTotalAmount();
    BigDecimal getAllocatedAmount();
    BigDecimal getSpentAmount();
}
