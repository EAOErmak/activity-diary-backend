package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.LoginEventRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final LoginEventRepository loginEventRepository;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("users", userRepository.count());
        stats.put("diaryEntries", diaryRepository.count());
        stats.put("loginEvents", loginEventRepository.count());

        return stats;
    }
}
