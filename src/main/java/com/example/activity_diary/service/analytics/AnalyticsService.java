package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.ChartResponseDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

public interface AnalyticsService {

    // ==== ПО ВРЕМЕНИ ====

    ChartResponseDto buildByTimeByCategory(
            UserDetails currentUser,
            Long whatHappenedId,
            LocalDateTime from,
            LocalDateTime to
    );

    ChartResponseDto buildByTimeByWhat(
            UserDetails currentUser,
            Long whatId,
            LocalDateTime from,
            LocalDateTime to
    );

    // ==== ПО ПОРЯДКУ (SEQUENCE) ====

    ChartResponseDto buildBySequenceByCategory(
            UserDetails currentUser,
            Long whatHappenedId,
            LocalDateTime from,
            LocalDateTime to
    );

    ChartResponseDto buildBySequenceByWhat(
            UserDetails currentUser,
            Long whatId,
            LocalDateTime from,
            LocalDateTime to
    );
}
