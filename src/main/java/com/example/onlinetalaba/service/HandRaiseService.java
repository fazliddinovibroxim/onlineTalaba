package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.HandRaiseDecisionRequest;
import com.example.onlinetalaba.dto.live.HandRaiseResponse;
import com.example.onlinetalaba.entity.*;
import com.example.onlinetalaba.enums.HandRaiseStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.HandRaiseRepository;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HandRaiseService {

    private final HandRaiseRepository handRaiseRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Transactional
    public HandRaiseResponse raiseHand(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        session.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        HandRaise handRaise = handRaiseRepository
                .findByLiveSessionIdAndUserIdAndActiveTrue(liveSessionId, currentUser.getId())
                .orElseGet(() -> HandRaise.builder()
                        .liveSession(session)
                        .user(currentUser)
                        .status(HandRaiseStatus.PENDING)
                        .active(true)
                        .requestedAt(LocalDateTime.now())
                        .build());

        handRaise.setStatus(HandRaiseStatus.PENDING);
        handRaise.setActive(true);
        handRaise.setRequestedAt(LocalDateTime.now());
        handRaise.setProcessedAt(null);
        handRaise.setProcessedBy(null);

        handRaiseRepository.save(handRaise);
        return mapToResponse(handRaise);
    }

    @Transactional
    public HandRaiseResponse decide(HandRaiseDecisionRequest request, User currentUser) {
        HandRaise handRaise = handRaiseRepository.findById(request.getHandRaiseId())
                .orElseThrow(() -> new NotFoundException("Hand raise not found"));

        if (!handRaise.getLiveSession().getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        handRaise.getLiveSession().getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        boolean canModerate = member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanManageRoom();

        if (!canModerate) {
            throw new ForbiddenException("You do not have permission to process hand raises");
        }

        handRaise.setStatus(request.getStatus());
        handRaise.setProcessedAt(LocalDateTime.now());
        handRaise.setProcessedBy(currentUser);
        handRaise.setActive(request.getStatus() == HandRaiseStatus.PENDING);

        handRaiseRepository.save(handRaise);
        return mapToResponse(handRaise);
    }

    @Transactional(readOnly = true)
    public List<HandRaiseResponse> list(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        session.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        return handRaiseRepository.findAllByLiveSessionIdAndActiveTrueOrderByRequestedAtAsc(liveSessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private HandRaiseResponse mapToResponse(HandRaise handRaise) {
        return HandRaiseResponse.builder()
                .id(handRaise.getId())
                .liveSessionId(handRaise.getLiveSession().getId())
                .userId(handRaise.getUser().getId())
                .userName(handRaise.getUser().getFullName())
                .status(handRaise.getStatus())
                .requestedAt(handRaise.getRequestedAt())
                .processedAt(handRaise.getProcessedAt())
                .build();
    }
}
