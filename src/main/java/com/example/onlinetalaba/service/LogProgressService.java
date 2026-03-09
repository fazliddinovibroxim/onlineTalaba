package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.log.LogProgressDto;
import com.example.onlinetalaba.entity.LogProgress;
import com.example.onlinetalaba.repository.LogProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogProgressService {

    private final LogProgressRepository logProgressRepository;

    public Page<LogProgressDto> getAll(LocalDate from, LocalDate to, Pageable pageable) {
        LocalDate fromDate = (from != null) ? from : LocalDate.now().minusDays(1);
        LocalDate toDate = (to != null) ? to : LocalDate.now();

        log.info("Fetching logs from {} to {}", fromDate, toDate);
        Page<LogProgress> pageData = logProgressRepository.findAllByDateRangeNative(fromDate, toDate, pageable);

        log.info("Fetched {} logs", pageData.getTotalElements());
        // Entity -> DTO mapping
        return pageData.map(this::toResponse);
    }

    public List<LogProgressDto> getByActionType(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            log.warn("Action type is blank, returning empty list");
            return List.of();
        }

        log.info("Fetching logs by action type: {}", actionType);
        List<LogProgress> results = logProgressRepository.findAllByActionTypeNative(actionType);
        log.info("Fetched {} logs for action type {}", results.size(), actionType);
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    // Universal mapping method
    public LogProgressDto toResponse(LogProgress log) {
        return LogProgressDto.builder()
                .action(log.getAction())
                .type(log.getType())
                .exception(log.getException())
                .build();
    }
}