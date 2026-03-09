package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.log.UserActionLogResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.entity.UserActionLog;
import com.example.onlinetalaba.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionLogService {

    private final UserActionLogRepository repository;

    public void log(
            User user,
            String requestedUrl,
            String actionType,
            String actionInfo
    ) {
        try {
            UserActionLog logEntry = UserActionLog.builder()
                    .user(user)
                    .requestedUrl(requestedUrl)
                    .actionType(actionType)
                    .actionInfo(actionInfo)
                    .build();

            repository.save(logEntry);
            log.info("UserActionLog created: userId={}, actionType={}, url={}", user.getId(), actionType, requestedUrl);
        } catch (Exception e) {
            log.error("Failed to save UserActionLog for userId={}, actionType={}, url={}",
                    user != null ? user.getId() : null,
                    actionType, requestedUrl, e);
            throw e;
        }
    }

    public List<UserActionLogResponse> getAll() {
        try {
            List<UserActionLogResponse> logs = repository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
            log.info("Fetched {} user action logs", logs.size());
            return logs;
        } catch (Exception e) {
            log.error("Failed to fetch all UserActionLogs", e);
            throw e;
        }
    }

    public List<UserActionLogResponse> getByUserId(Long userId) {
        try {
            List<UserActionLogResponse> logs = repository.findByUserId(userId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
            log.info("Fetched {} logs for userId={}", logs.size(), userId);
            return logs;
        } catch (Exception e) {
            log.error("Failed to fetch UserActionLogs for userId={}", userId, e);
            throw e;
        }
    }

    private UserActionLogResponse toResponse(UserActionLog entity) {
        User user = entity.getUser();
        return UserActionLogResponse.builder()
                .id(entity.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getFullName() : "SYSTEM")
                .requestedUrl(entity.getRequestedUrl())
                .actionInfo(entity.getActionInfo())
                .actionType(entity.getActionType())
                .time(entity.getTime())
                .build();
    }
}