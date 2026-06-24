package com.cuea.rmp.reporting.infrastructure.persistence;

public interface SkillSupplyProjection {
    String getSkillId();
    String getSkillName();
    long getResourceCount();
    double getAvgProficiency();
}
