package com.cuea.rmp.timesheet.application.port.out;

import java.util.UUID;

/**
 * Outbound port the timesheet feature calls when a timesheet is approved, so the
 * owning project's budget spend can be recalculated. Implemented by the budget
 * feature (M8). Defined here (caller-owns) to keep feature dependencies acyclic.
 */
public interface BudgetRecalculationPort {

    void recalculateForAssignment(UUID assignmentId);
}
