package com.cuea.rmp.request.infrastructure.notification;

import com.cuea.rmp.request.application.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub {@link NotificationPort} for M6 — logs instead of sending. The real
 * Firebase FCM adapter (M9) should replace this (e.g. mark its bean {@code @Primary}
 * or remove this stub) to avoid a duplicate-bean conflict.
 */
@Component
public class StubNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(StubNotificationAdapter.class);

    @Override
    public void notify(UUID recipientId, String message) {
        log.info("[NOTIFY:stub] -> user {} : {}", recipientId, message);
    }
}
