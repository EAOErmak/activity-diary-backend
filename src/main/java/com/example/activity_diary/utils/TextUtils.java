package com.example.activity_diary.utils;

public final class TextUtils {

    private TextUtils() {
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}
