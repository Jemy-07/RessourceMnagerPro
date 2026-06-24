package com.cuea.rmp.budget.application.port.in;

import java.util.UUID;

public interface RecalculateSpendUseCase {
    /** Recomputes a project's spend from approved timesheets. No-op if the project has no budget. */
    void recalculate(UUID projectId);
}
