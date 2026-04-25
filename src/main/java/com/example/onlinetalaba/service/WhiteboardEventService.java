package com.example.onlinetalaba.service;

import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.dto.live.WhiteboardEventRequest;
import com.example.onlinetalaba.dto.live.WhiteboardEventResponse;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.entity.WhiteboardEvent;
import com.example.onlinetalaba.enums.LiveSessionStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.WhiteboardEventType;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.WhiteboardEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhiteboardEventService {

    private final WhiteboardEventRepository whiteboardEventRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    @Transactional
    public WhiteboardEventResponse saveEvent(WhiteboardEventRequest request, User currentUser) {
        LiveSession liveSession = liveSessionRepository.findById(request.getLiveSessionId())
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!liveSession.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        if (!liveSession.isActive() || liveSession.getStatus() != LiveSessionStatus.LIVE) {
            throw new ForbiddenException("Live session is not active");
        }

        roomService.validateMemberAccess(liveSession.getLessonSchedule().getRoom(), currentUser);

        if (request.getEventType() == null) {
            throw new BadRequestException("Event type is required");
        }

        if (request.getEventType() == WhiteboardEventType.TOGGLE) {
            RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                    liveSession.getLessonSchedule().getRoom().getId(),
                    currentUser.getId()
            ).orElse(null);

            boolean isSuperAdmin = currentUser.getRoles() != null
                    && currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN;

            boolean isHost = liveSession.getHost() != null && liveSession.getHost().getId().equals(currentUser.getId());

            boolean canToggle = isSuperAdmin
                    || isHost
                    || (member != null && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER));

            if (!canToggle) {
                throw new ForbiddenException("Only teacher can toggle whiteboard");
            }

            boolean enabled = parseToggleEnabled(request.getPayloadJson());
            liveSession.getLessonSchedule().setWhiteboardEnabled(enabled);
            lessonScheduleRepository.save(liveSession.getLessonSchedule());
        } else {
            if (!liveSession.getLessonSchedule().isWhiteboardEnabled()) {
                throw new ForbiddenException("Whiteboard is disabled");
            }
        }

        WhiteboardEvent event = WhiteboardEvent.builder()
                .liveSession(liveSession)
                .sender(currentUser)
                .eventType(request.getEventType())
                .payloadJson(request.getPayloadJson())
                .deleted(false)
                .build();

        whiteboardEventRepository.save(event);
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<WhiteboardEventResponse> history(Long liveSessionId, User currentUser) {
        LiveSession liveSession = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!liveSession.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        if (!liveSession.isActive() || liveSession.getStatus() != LiveSessionStatus.LIVE) {
            throw new ForbiddenException("Live session is not active");
        }

        roomService.validateMemberAccess(liveSession.getLessonSchedule().getRoom(), currentUser);

        return whiteboardEventRepository.findAllByLiveSessionIdAndDeletedFalseOrderByIdAsc(liveSessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WhiteboardEventResponse mapToResponse(WhiteboardEvent event) {
        return WhiteboardEventResponse.builder()
                .id(event.getId())
                .liveSessionId(event.getLiveSession().getId())
                .senderId(event.getSender().getId())
                .senderName(event.getSender().getFullName())
                .eventType(event.getEventType())
                .payloadJson(event.getPayloadJson())
                .createdAt(event.getDatetimeCreated())
                .build();
    }

    private boolean parseToggleEnabled(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new BadRequestException("payloadJson is required for TOGGLE event");
        }

        String raw = payloadJson.trim();
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.parseBoolean(raw);
        }

        try {
            JsonNode node = objectMapper.readTree(raw);
            JsonNode enabledNode = node.get("enabled");
            if (enabledNode == null || !enabledNode.isBoolean()) {
                throw new BadRequestException("TOGGLE payloadJson must contain boolean field 'enabled'");
            }
            return enabledNode.asBoolean();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Invalid TOGGLE payloadJson: " + e.getMessage());
        }
    }
}
