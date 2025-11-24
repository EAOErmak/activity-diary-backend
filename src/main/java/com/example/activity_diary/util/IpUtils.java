package com.example.activity_diary.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    /**
     * Берём реальный IP с учётом прокси/nginx (если есть),
     * но без риска подмены — берём только первый элемент.
     */
    public static String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For: client, proxy1, proxy2 ...
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }
}
