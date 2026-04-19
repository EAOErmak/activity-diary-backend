package com.example.activity_diary.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    /**
     * Р‘РµСЂС‘Рј СЂРµР°Р»СЊРЅС‹Р№ IP СЃ СѓС‡С‘С‚РѕРј РїСЂРѕРєСЃРё/nginx (РµСЃР»Рё РµСЃС‚СЊ),
     * РЅРѕ Р±РµР· СЂРёСЃРєР° РїРѕРґРјРµРЅС‹ вЂ” Р±РµСЂС‘Рј С‚РѕР»СЊРєРѕ РїРµСЂРІС‹Р№ СЌР»РµРјРµРЅС‚.
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
