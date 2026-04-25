package com.example.onlinetalaba.config;

import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.LiveSessionStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.handler.UnauthorizedException;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket inbound guard for livestream signaling:
 * - Only room members can SEND to /app/stream/{sessionId}/...
 * - Only host/teacher can SUBSCRIBE to /topic/stream/{sessionId}/participants
 */
@Component
@RequiredArgsConstructor
public class LiveStreamMembershipChannelInterceptor implements ChannelInterceptor {

    private static final Pattern STREAM_DEST = Pattern.compile("^/(app|topic)/stream/(?<id>\\d+)(/.*)?$");

    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand cmd = accessor.getCommand();
        if (cmd != StompCommand.SEND && cmd != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            return message;
        }

        Matcher m = STREAM_DEST.matcher(destination);
        if (!m.matches()) {
            return message;
        }

        Long liveSessionId = Long.valueOf(m.group("id"));

        User currentUser = resolveUser(accessor);
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        // For SEND we require the session to be live; for SUBSCRIBE we allow teacher to open UI before start if needed.
        if (cmd == StompCommand.SEND) {
            if (!session.isActive() || session.getStatus() != LiveSessionStatus.LIVE) {
                throw new ForbiddenException("Live session is not active");
            }
        }

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(session.getLessonSchedule().getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (cmd == StompCommand.SUBSCRIBE && isParticipantsTopic(destination)) {
            boolean canViewParticipants = isSuperAdmin(currentUser)
                    || member.getRole() == RoomMemberRole.OWNER
                    || member.getRole() == RoomMemberRole.TEACHER
                    || (session.getHost() != null && session.getHost().getId().equals(currentUser.getId()));

            if (!canViewParticipants) {
                throw new ForbiddenException("Only teacher can view live participants");
            }
        }

        return message;
    }

    private boolean isParticipantsTopic(String destination) {
        return destination.toLowerCase(Locale.ROOT).endsWith("/participants");
    }

    private boolean isSuperAdmin(User user) {
        return user.getRoles() != null && user.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN;
    }

    private User resolveUser(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof StompPrincipal stompPrincipal)) {
            throw new UnauthorizedException("Unauthorized websocket user");
        }
        return stompPrincipal.getUser();
    }
}

