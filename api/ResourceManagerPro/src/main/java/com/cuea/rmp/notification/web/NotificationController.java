package com.cuea.rmp.notification.web;

import com.cuea.rmp.auth.infrastructure.security.CurrentUserProvider;
import com.cuea.rmp.notification.application.dto.NotificationResult;
import com.cuea.rmp.notification.application.port.in.ListNotificationsUseCase;
import com.cuea.rmp.notification.application.port.in.MarkAsReadUseCase;
import com.cuea.rmp.notification.web.response.NotificationResponse;
import com.cuea.rmp.shared.application.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final ListNotificationsUseCase listNotifications;
    private final MarkAsReadUseCase markAsRead;
    private final CurrentUserProvider currentUser;

    public NotificationController(ListNotificationsUseCase listNotifications,
                                  MarkAsReadUseCase markAsRead,
                                  CurrentUserProvider currentUser) {
        this.listNotifications = listNotifications;
        this.markAsRead = markAsRead;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list() {
        return ApiResponse.ok(listNotifications.list(currentUser.currentUserId()).stream()
                .map(this::toResponse).toList());
    }

    @PostMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID id) {
        NotificationResult result = markAsRead.markAsRead(id, currentUser.currentUserId());
        return ApiResponse.ok(toResponse(result), "Notification marked read");
    }

    private NotificationResponse toResponse(NotificationResult r) {
        return new NotificationResponse(r.id(), r.userId(), r.type(), r.message(), r.read());
    }
}
