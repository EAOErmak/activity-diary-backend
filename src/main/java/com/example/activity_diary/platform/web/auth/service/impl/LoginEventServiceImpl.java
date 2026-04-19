package com.example.activity_diary.platform.web.auth.service.impl;

import com.example.activity_diary.entity.log.LoginEvent;
import com.example.activity_diary.repository.LoginEventRepository;
import com.example.activity_diary.platform.web.auth.service.LoginEventService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service

@RequiredArgsConstructor
public class LoginEventServiceImpl implements LoginEventService {

    private final LoginEventRepository repo;

    @Override
    public void recordSuccess(Long userId, String ip, String userAgent) {

        if (ip == null || ip.isBlank()) {
            ip = "UNKNOWN";
        }

        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "UNKNOWN";
        }

        LoginEvent e = LoginEvent.builder()
                .userId(userId)
                .ip(ip)
                .userAgent(userAgent)
                .success(true)
                .build();

        repo.save(e);
    }

    @Override
    public void recordFailure(String ip, String userAgent) {

        if (ip == null || ip.isBlank()) {
            ip = "UNKNOWN";
        }

        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "UNKNOWN";
        }

        LoginEvent e = LoginEvent.builder()
                .userId(null)
                .ip(ip)
                .userAgent(userAgent)
                .success(false)
                .build();

        repo.save(e);
    }
}
