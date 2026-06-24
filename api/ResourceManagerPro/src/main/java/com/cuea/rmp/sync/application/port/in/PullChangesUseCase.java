package com.cuea.rmp.sync.application.port.in;

import com.cuea.rmp.sync.application.dto.PullResult;

import java.time.Instant;

public interface PullChangesUseCase {
    PullResult pull(Instant since);
}
