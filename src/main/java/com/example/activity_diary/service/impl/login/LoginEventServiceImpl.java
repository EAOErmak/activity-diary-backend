package com.example.activity_diary.service.impl.login;

import com.example.activity_diary.entity.log.LoginEvent;
import com.example.activity_diary.repository.LoginEventRepository;
import com.example.activity_diary.service.login.LoginEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginEventServiceImpl implements LoginEventService {

    private final LoginEventRepository repo;

    @Override
    public void recordSuccess(Long userId, String ip, String userAgent) {
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
        LoginEvent e = LoginEvent.builder()
                .userId(null)
                .ip(ip)
                .userAgent(userAgent)
                .success(false)
                .build();
        repo.save(e);
    }
}
