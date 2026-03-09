package com.example.onlinetalaba.notification;

import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/createToken")
    public ResponseEntity<?> createToken(@RequestParam String fcmToken, @CurrentUser User user) {
        return notificationService.setFcmToken(user, fcmToken);
    }

    @GetMapping("/publicForTest")
    public CompletableFuture<ResponseEntity<String>> getPublicForTest(String token, String body, String title ) {
        try {
            return notificationService.basicSendNotification(title, body, token);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/markAsRead/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @CurrentUser User user) {
        return notificationService.markAsRead(id, user);
    }

    @PutMapping("/markAllAsRead")
    public ResponseEntity<?> markAllAsRead(@CurrentUser User user) {
        return notificationService.markAllAsRead(user);
    }

    @GetMapping("/getAllNotificationByUserId")
    public ResponseEntity<?> getAllNotificationByUserId(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                        @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
                                                        @RequestParam("userId") Long userId) {
        return notificationService.getAllNotificationByUserId(PageRequest.of(page, size), userId);
    }

    @DeleteMapping("/deleteToken")
    public ResponseEntity<?> deleteToken(@RequestParam String fcmToken, @CurrentUser User user) {
        return notificationService.deleteFcmToken(user, fcmToken);
    }

}
