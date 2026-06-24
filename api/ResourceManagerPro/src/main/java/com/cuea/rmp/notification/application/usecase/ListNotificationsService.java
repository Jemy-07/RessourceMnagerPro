package com.cuea.rmp.notification.application.usecase;

import com.cuea.rmp.notification.application.dto.NotificationResult;
import com.cuea.rmp.notification.application.port.in.ListNotificationsUseCase;
import com.cuea.rmp.notification.application.port.out.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListNotificationsService implements ListNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public ListNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResult> list(UUID userId) {
        return notificationRepository.findByUserId(userId).stream().map(NotificationResult::from).toList();
    }
}
