package com.example.onlinetalaba.notification;

import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.ErrorCodes;
import com.example.onlinetalaba.handler.ErrorMessageException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final FCMRepository fcmRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationSocketService notificationSocketService;


    public ResponseEntity<?> setFcmToken(User user, String fcmToken) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return ResponseEntity.badRequest().body("FCM token cannot be empty");
        }

        Optional<FCM> existingByToken = fcmRepository.findByFcmToken(fcmToken);
        if (existingByToken.isPresent()) {
            FCM existing = existingByToken.get();

            if (existing.getUser().getId().equals(user.getId())) {
                return ResponseEntity.ok("Token already registered for this user");
            }
            return ResponseEntity.badRequest().body("This token is already registered for another user");
        }

        Optional<FCM> existingByUser = fcmRepository.findByUserId(user.getId());
        FCM token = existingByUser
                .map(t -> {
                    t.setFcmToken(fcmToken);
                    return t;
                })
                .orElseGet(() -> FCM.builder()
                        .user(user)
                        .fcmToken(fcmToken)
                        .build()
                );

        // Save
        fcmRepository.save(token);

        return ResponseEntity.ok("Token has been created successfully");
    }


//    public void sendIncomeNotification(Double quantity ,User user, String body) {
//        String title = "Kirim " + quantity;
//        Notification notification = Notification.builder()
//                .user(user)
//                .title(title)
//                .body(body)
//                .status("PENDING")
//                .createdAt(LocalDate.now())
//                .build();
//
//        notificationRepository.save(notification);
//        sendAsyncNotification(notification);
//    }

    @Async("NotificationTaskExecutor")
    public CompletableFuture<ResponseEntity<?>> sendAsyncNotification(Notification notification) {
        Long userId = notification.getUser().getId();
        Optional<FCM> maybeToken = fcmRepository.findByUserId(userId);
        if (maybeToken.isEmpty()) {
            notification.setStatus("NO_TOKEN");
            notificationRepository.save(notification);
            return CompletableFuture.completedFuture(null);
        }

        FCM maybe = maybeToken.get();

        if (maybe.getFcmToken() == null) {
            notification.setStatus("NO_TOKEN");
            notificationRepository.save(notification);
            return CompletableFuture.completedFuture(null);
        }

        try {
            Message message = Message.builder()
                    .setToken(maybe.getFcmToken()) // ✅ access user token
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getBody())
                        .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            notification.setStatus("SENT");
            notificationRepository.save(notification);
            return CompletableFuture.completedFuture(ResponseEntity.ok().body(response));
        } catch (Exception ex) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
        }

        notificationRepository.save(notification);
        return CompletableFuture.completedFuture(null);

    }

    @Transactional
    public Notification createAndDispatch(User user, String title, String body) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .status("PENDING")
                .createdAt(LocalDate.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        notificationSocketService.pushToUser(notificationMapper.toDto(notification), user);
        sendAsyncNotification(notification);
        return notification;
    }

    @Transactional
    public void createAndDispatch(List<User> users, String title, String body) {
        users.forEach(user -> createAndDispatch(user, title, body));
    }

    public CompletableFuture<ResponseEntity<String>> basicSendNotification(String title, String body, String token) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        String response = FirebaseMessaging.getInstance().send(message);
        return CompletableFuture.completedFuture(ResponseEntity.ok().body(response));
    }

    @Transactional
    public ResponseEntity<?> markAsRead(Long id,User user) {
        Notification notification = notificationRepository.findById(id).orElseThrow(()
                -> new ErrorMessageException("Not found", ErrorCodes.NotFound));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ErrorMessageException("Access denied", ErrorCodes.Unauthorized);
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read");
    }
    @Transactional
    public ResponseEntity<?> markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    public ResponseEntity<?> getAllNotificationByUserId(Pageable pageable, Long userId) {
        Page<Notification> page = notificationRepository.findAllByUserId(userId, pageable);
        if (!page.isEmpty()) {
            return ResponseEntity.ok(notificationMapper.dtoList(page.getContent()));
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    public ResponseEntity<?> deleteFcmToken(User user, String fcmToken) {
        Optional<FCM> existing = fcmRepository.findByFcmToken(fcmToken);

        if (existing.isEmpty() || !existing.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("FCM token not found for this user");
        }

        fcmRepository.delete(existing.get());
        return ResponseEntity.ok("FCM token deleted successfully");
    }



}
