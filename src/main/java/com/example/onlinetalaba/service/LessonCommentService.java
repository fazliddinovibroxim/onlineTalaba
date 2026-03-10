package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.chat.LessonCommentRequest;
import com.example.onlinetalaba.dto.chat.LessonCommentResponse;
import com.example.onlinetalaba.entity.LessonCommentMessage;
import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.LessonCommentType;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LessonCommentMessageRepository;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonCommentService {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LessonCommentMessageRepository lessonCommentMessageRepository;

    @Transactional
    public LessonCommentResponse send(LessonCommentRequest request, User currentUser) {
        LessonSchedule lesson = lessonScheduleRepository.findById(request.getLessonScheduleId())
                .orElseThrow(() -> new NotFoundException("Lesson schedule not found"));

        if (!lesson.getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        lesson.getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!member.isActive()) {
            throw new ForbiddenException("Inactive room member");
        }

        LessonCommentMessage message = LessonCommentMessage.builder()
                .lessonSchedule(lesson)
                .sender(currentUser)
                .content(request.getContent())
                .commentType(request.getCommentType() == null ? LessonCommentType.COMMENT : request.getCommentType())
                .edited(false)
                .deleted(false)
                .build();

        lessonCommentMessageRepository.save(message);

        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public List<LessonCommentResponse> getLessonComments(Long lessonScheduleId, User currentUser) {
        LessonSchedule lesson = lessonScheduleRepository.findById(lessonScheduleId)
                .orElseThrow(() -> new NotFoundException("Lesson schedule not found"));

        if (!lesson.getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(lesson.getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        return lessonCommentMessageRepository.findAllByLessonScheduleIdAndDeletedFalseOrderByIdAsc(lessonScheduleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LessonCommentResponse mapToResponse(LessonCommentMessage message) {
        return LessonCommentResponse.builder()
                .id(message.getId())
                .lessonScheduleId(message.getLessonSchedule().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderEmail(message.getSender().getEmail())
                .content(message.getContent())
                .commentType(message.getCommentType())
                .edited(message.isEdited())
                .createdAt(message.getDatetimeCreated())
                .build();
    }
}
