package com.example.activity_diary.service.login;

public interface LoginEventService {

    void recordSuccess(Long userId, String ip, String userAgent);

    void recordFailure(String ip, String userAgent);
}
