package com.example.activity_diary.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils {

    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            return "Unknown";
        }
        return ua;
    }
}
