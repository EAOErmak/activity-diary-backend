package com.example.activity_diary.rate;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitService {

    boolean tryGlobalConsume(HttpServletRequest request);

    boolean tryConsumeForAnnotation(HttpServletRequest request, RateLimit annotation);
}
