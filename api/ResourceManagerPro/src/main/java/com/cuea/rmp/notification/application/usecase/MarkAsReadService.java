package com.cuea.rmp.notification.application.usecase;

import com.cuea.rmp.notification.application.dto.NotificationResult;
import com.cuea.rmp.notification.application.port.in.MarkAsReadUseCase;
import com.cuea.rmp.notification.application.port.out.NotificationRepository;
import com.cuea.rmp.notification.domain.Notification;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MarkAsReadService implements MarkAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkAsReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationResult markAsRead(UUID id, UUID userId) {
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Notification " + id + " not found"));
        notification.markAsRead();
        return NotificationResult.from(notificationRepository.save(notification));
    }
}
