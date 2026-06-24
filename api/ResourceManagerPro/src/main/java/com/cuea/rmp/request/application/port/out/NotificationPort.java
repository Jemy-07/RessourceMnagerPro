package com.cuea.rmp.request.application.port.out;

import java.util.UUID;

/**
 * Outbound port for sending notifications. Stubbed for M6 (logs only); a real
 * implementation (Firebase FCM) arrives in M9.
 */
public interface NotificationPort {

    void notify(UUID recipientId, String message);
}
