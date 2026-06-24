package com.cuea.rmp.reporting.infrastructure.persistence;

/** Interface projection for the utilisation native query (ids as String). */
public interface UtilizationProjection {
    String getResourceId();
    String getResourceName();
    long getActiveAssignments();
    long getAllocatedPct();
}
