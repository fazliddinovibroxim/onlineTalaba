package com.example.onlinetalaba.component;

import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.enums.LessonStatus;
import com.example.onlinetalaba.notification.NotificationService;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LessonReminderScheduler {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void sendUpcomingLessonReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(15);

        for (LessonSchedule lesson : lessonScheduleRepository
                .findAllByStatusAndStartTimeBetweenAndReminderSentAtIsNull(LessonStatus.SCHEDULED, now, windowEnd)) {

            roomMemberRepository.findAllByRoomIdAndActiveTrue(lesson.getRoom().getId())
                    .stream()
                    .map(member -> member.getUser())
                    .forEach(user -> notificationService.createAndDispatch(
                            user,
                            "Dars eslatmasi",
                            lesson.getTitle() + " darsi " + lesson.getStartTime() + " da boshlanadi"
                    ));

            lesson.setReminderSentAt(LocalDateTime.now());
            lessonScheduleRepository.save(lesson);
        }
    }
}
