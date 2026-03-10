package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.WhiteboardEventRequest;
import com.example.onlinetalaba.dto.live.WhiteboardEventResponse;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.entity.WhiteboardEvent;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.WhiteboardEventRepository;
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

    @Transactional
    public WhiteboardEventResponse saveEvent(WhiteboardEventRequest request, User currentUser) {
        LiveSession liveSession = liveSessionRepository.findById(request.getLiveSessionId())
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!liveSession.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        liveSession.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!member.isActive()) {
            throw new ForbiddenException("Inactive room member");
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

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        liveSession.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

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
}
