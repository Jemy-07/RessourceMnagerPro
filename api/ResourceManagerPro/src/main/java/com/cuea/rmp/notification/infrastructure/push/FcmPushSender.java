package com.cuea.rmp.notification.infrastructure.push;

import com.cuea.rmp.notification.application.port.out.PushSender;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Firebase Cloud Messaging implementation of {@link PushSender}.
 * <p>
 * Initialises a dedicated {@link FirebaseApp} from the service-account JSON at
 * {@code app.fcm.service-account-path}. If the property is blank or the file is
 * missing (dev mode), FCM is disabled and {@link #send} is a logged no-op. All
 * delivery errors are swallowed so notification persistence is never affected.
 */
@Component
public class FcmPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);
    private static final String APP_NAME = "rmp-fcm";

    private final String serviceAccountPath;
    private FirebaseApp firebaseApp;

    public FcmPushSender(@Value("${app.fcm.service-account-path:}") String serviceAccountPath) {
        this.serviceAccountPath = serviceAccountPath;
    }

    @PostConstruct
    void init() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("[FCM] No app.fcm.service-account-path configured — push notifications DISABLED (dev mode).");
            return;
        }
        Path path = Path.of(serviceAccountPath);
        if (!Files.exists(path)) {
            log.warn("[FCM] Service-account file not found at {} — push notifications DISABLED.", serviceAccountPath);
            return;
        }
        try (InputStream credentials = new FileInputStream(path.toFile())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            this.firebaseApp = FirebaseApp.getApps().stream()
                    .filter(a -> APP_NAME.equals(a.getName()))
                    .findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(options, APP_NAME));
            log.info("[FCM] Initialised — push notifications ENABLED.");
        } catch (Exception ex) {
            log.warn("[FCM] Failed to initialise ({}) — push notifications DISABLED.", ex.getMessage());
            this.firebaseApp = null;
        }
    }

    @Override
    public void send(String fcmToken, String title, String body) {
        if (firebaseApp == null) {
            log.debug("[FCM] disabled; skipping push to token={} title={}", abbreviate(fcmToken), title);
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String messageId = FirebaseMessaging.getInstance(firebaseApp).send(message);
            log.debug("[FCM] sent push id={} to token={}", messageId, abbreviate(fcmToken));
        } catch (Exception ex) {
            // Fail soft: a bad/expired token must not break the notify() flow.
            log.warn("[FCM] push failed for token={}: {}", abbreviate(fcmToken), ex.getMessage());
        }
    }

    private static String abbreviate(String token) {
        if (token == null || token.length() <= 8) {
            return token;
        }
        return token.substring(0, 8) + "…";
    }
}
