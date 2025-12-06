package com.example.activity_diary.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long activeToday;
    private long blockedUsers;
    private long newUsersLast7Days;

    private long totalEntries;
    private long entriesToday;
    private long entriesLast7Days;
}

