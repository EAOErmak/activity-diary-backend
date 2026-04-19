package com.example.activity_diary.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MetricValueNormalizer {

    public static final int SCALE = 5;

    private MetricValueNormalizer() {
    }

    public static BigDecimal normalizePositive(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }

        BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }

        return normalized;
    }
}
