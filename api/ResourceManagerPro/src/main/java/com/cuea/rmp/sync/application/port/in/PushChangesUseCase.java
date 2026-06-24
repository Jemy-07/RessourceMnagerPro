package com.cuea.rmp.sync.application.port.in;

import com.cuea.rmp.sync.application.dto.PushResult;
import com.cuea.rmp.sync.domain.ChangeSet;

public interface PushChangesUseCase {
    PushResult push(ChangeSet changeSet);
}
