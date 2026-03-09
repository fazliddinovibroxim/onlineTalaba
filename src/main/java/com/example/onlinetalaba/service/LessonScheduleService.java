package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.schedule.LessonScheduleRequest;
import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.LessonStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonScheduleService {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Transactional
    public void create(Long roomId, LessonScheduleRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (!(member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanScheduleLesson())) {
            throw new RuntimeException("You do not have permission to schedule lesson");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
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
}