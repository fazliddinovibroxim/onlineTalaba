package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.LiveSessionCommentRequest;
import com.example.onlinetalaba.dto.live.LiveSessionCommentResponse;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.LiveSessionComment;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.LessonCommentType;
import com.example.onlinetalaba.enums.LiveSessionStatus;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LiveSessionCommentRepository;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveSessionCommentService {

    private static final int MAX_CONTENT_LENGTH = 5000;

    private final LiveSessionRepository liveSessionRepository;
    private final LiveSessionCommentRepository liveSessionCommentRepository;
    private final RoomService roomService;
    private final RoomNotificationService roomNotificationService;

    @Transactional
    public LiveSessionCommentResponse send(Long liveSessionId, LiveSessionCommentRequest request, User currentUser) {
        LiveSession session = getJoinableLiveSession(liveSessionId);
        validateCommentsEnabled(session);
        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        String content = normalizeContent(request != null ? request.getContent() : null);
        LessonCommentType commentType = request != null && request.getCommentType() != null
                ? request.getCommentType()
                : LessonCommentType.COMMENT;

        LiveSessionComment comment = LiveSessionComment.builder()
                .liveSession(session)
                .sender(currentUser)
                .content(content)
                .commentType(commentType)
                .edited(false)
                .deleted(false)
                .build();

        liveSessionCommentRepository.save(comment);
        LiveSessionCommentResponse response = mapToResponse(comment);

        roomNotificationService.notifyStreamComment(
                session.getLessonSchedule().getRoom().getId(),
                session.getId(),
                session.getLessonSchedule().getId(),
                currentUser,
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<LiveSessionCommentResponse> getComments(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        return liveSessionCommentRepository
                .findAllByLiveSessionIdAndDeletedFalseOrderByIdAsc(liveSessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LiveSession getJoinableLiveSession(Long liveSessionId) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        if (!session.isActive() || session.getStatus() != LiveSessionStatus.LIVE) {
            throw new ForbiddenException("Live session is not active");
        }
        return session;
    }

    private void validateCommentsEnabled(LiveSession session) {
        if (!session.getLessonSchedule().isLiveCommentsEnabled()) {
            throw new ForbiddenException("Live comments are disabled for this lesson");
        }
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Comment content is required");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BadRequestException("Comment content is too long");
        }
        return trimmed;
    }

    private LiveSessionCommentResponse mapToResponse(LiveSessionComment comment) {
        User sender = comment.getSender();
        LiveSession session = comment.getLiveSession();
        String displayName = sender.getFullName() != null && !sender.getFullName().isBlank()
                ? sender.getFullName()
                : sender.getUsername();

        return LiveSessionCommentResponse.builder()
                .id(comment.getId())
                .liveSessionId(session.getId())
                .lessonScheduleId(session.getLessonSchedule().getId())
                .roomId(session.getLessonSchedule().getRoom().getId())
                .senderId(sender.getId())
                .senderName(displayName)
                .senderUsername(sender.getUsername())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .edited(comment.isEdited())
                .createdAt(comment.getDatetimeCreated())
                .build();
    }
}
