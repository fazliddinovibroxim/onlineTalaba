package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.schedule.LessonScheduleRequest;
import com.example.onlinetalaba.dto.schedule.LessonScheduleResponse;
import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.LessonStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonScheduleService {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Transactional
    public void create(Long roomId, LessonScheduleRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!(member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanScheduleLesson())) {
            throw new ForbiddenException("You do not have permission to schedule lesson");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        LessonSchedule lesson = LessonSchedule.builder()
                .room(room)
                .teacher(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(LessonStatus.SCHEDULED)
                .whiteboardEnabled(request.isWhiteboardEnabled())
                .liveCommentsEnabled(request.isLiveCommentsEnabled())
                .liveVoiceQuestionsEnabled(request.isLiveVoiceQuestionsEnabled())
                .build();

        lessonScheduleRepository.save(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonScheduleResponse> getAllByRoom(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        return lessonScheduleRepository.findAllByRoomIdOrderByStartTimeAsc(roomId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LessonScheduleResponse toResponse(LessonSchedule lesson) {
        return LessonScheduleResponse.builder()
                .id(lesson.getId())
                .roomId(lesson.getRoom().getId())
                .teacherId(lesson.getTeacher().getId())
                .teacherName(lesson.getTeacher().getFullName())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .status(lesson.getStatus())
                .whiteboardEnabled(lesson.isWhiteboardEnabled())
                .liveCommentsEnabled(lesson.isLiveCommentsEnabled())
                .liveVoiceQuestionsEnabled(lesson.isLiveVoiceQuestionsEnabled())
                .build();
    }
}
