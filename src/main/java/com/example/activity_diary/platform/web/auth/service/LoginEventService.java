package com.example.activity_diary.platform.web.auth.service;

public interface LoginEventService {

    void recordSuccess(Long userId, String ip, String userAgent);

    void recordFailure(String ip, String userAgent);
}
