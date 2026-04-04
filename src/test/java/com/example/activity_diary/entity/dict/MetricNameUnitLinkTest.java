package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.enums.DictionaryType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricNameUnitLinkTest {

    @Test
    void create_requiresMetricNameTypeOnLeftSide() {
        DictionaryItem left = dictionaryItem(DictionaryType.METRIC_UNIT, "kg");
        DictionaryItem right = dictionaryItem(DictionaryType.METRIC_UNIT, "lb");

        assertThrows(IllegalArgumentException.class, () -> MetricNameUnitLink.create(left, right));
    }

    @Test
    void create_requiresMetricUnitTypeOnRightSide() {
        DictionaryItem left = dictionaryItem(DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem right = dictionaryItem(DictionaryType.METRIC_NAME, "Distance");

        assertThrows(IllegalArgumentException.class, () -> MetricNameUnitLink.create(left, right));
    }

    private static DictionaryItem dictionaryItem(DictionaryType type, String label) {
        return DictionaryItem.builder()
                .type(type)
                .label(label)
                .active(true)
                .build();
    }
}
