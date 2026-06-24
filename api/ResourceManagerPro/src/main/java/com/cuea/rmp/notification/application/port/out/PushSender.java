package com.cuea.rmp.notification.application.port.out;

/**
 * Outbound port for delivering a push message to a single device token.
 * Implementations must fail soft (never throw) so notification persistence is
 * unaffected by push delivery problems.
 */
public interface PushSender {

    void send(String fcmToken, String title, String body);
}
