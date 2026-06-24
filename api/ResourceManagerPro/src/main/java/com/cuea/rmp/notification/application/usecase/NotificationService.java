package com.cuea.rmp.notification.application.usecase;

import com.cuea.rmp.notification.application.port.in.NotificationPort;
import com.cuea.rmp.notification.application.port.out.DeviceTokenRepository;
import com.cuea.rmp.notification.application.port.out.NotificationRepository;
import com.cuea.rmp.notification.application.port.out.PushSender;
import com.cuea.rmp.notification.domain.DeviceToken;
import com.cuea.rmp.notification.domain.Notification;
import com.cuea.rmp.notification.domain.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link NotificationPort}: persists the notification, then
 * pushes it to each of the user's registered devices (best-effort).
 */
@Service
@Transactional
public class NotificationService implements NotificationPort {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    public NotificationService(NotificationRepository notificationRepository,
                               DeviceTokenRepository deviceTokenRepository,
                               PushSender pushSender) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushSender = pushSender;
    }

    @Override
    public void notify(UUID userId, NotificationType type, String message) {
        Notification notification = Notification.create(userId, type, message);
        notificationRepository.save(notification);

        for (DeviceToken device : deviceTokenRepository.findByUserId(userId)) {
            pushSender.send(device.getFcmToken(), type.name(), message);
        }
    }
}
