package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.service.admin.AdminDashboardService;
import com.example.activity_diary.dto.admin.AdminDashboardStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryEntryRepository;

    @Override
    public AdminDashboardStatsDto getStats() {

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        Instant startOfToday =
                today.atStartOfDay(zone).toInstant();

        Instant startOfTomorrow =
                today.plusDays(1).atStartOfDay(zone).toInstant();

        Instant startOf7DaysAgo =
                today.minusDays(6).atStartOfDay(zone).toInstant();

        long totalUsers = userRepository.count();
        long blockedUsers = userRepository.countByAccountLockedTrue();

        long newUsersLast7Days =
                userRepository.countByCreatedAtAfter(startOf7DaysAgo);

        long totalEntries =
                diaryEntryRepository.count();

        long entriesToday =
                diaryEntryRepository.countEntriesBetween(
                        startOfToday, startOfTomorrow
                );

        long entriesLast7Days =
                diaryEntryRepository.countEntriesBetween(
                        startOf7DaysAgo, startOfTomorrow
                );

        long activeToday =
                diaryEntryRepository.countActiveUsersBetween(
                        startOfToday, startOfTomorrow
                );

        return new AdminDashboardStatsDto(
                totalUsers,
                activeToday,
                blockedUsers,
                newUsersLast7Days,
                totalEntries,
                entriesToday,
                entriesLast7Days
        );
    }
}
