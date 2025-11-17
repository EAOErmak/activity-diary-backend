package com.example.activity_diary.config;

import com.example.activity_diary.repository.TelegramSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final TelegramSessionRepository sessionRepository;

    // Run every minute
    @Scheduled(fixedRate = 60_000)
    public void clearExpiredSessions() {
        int deleted = sessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Expired Telegram sessions cleaned: {}", deleted);
        }
    }
}
