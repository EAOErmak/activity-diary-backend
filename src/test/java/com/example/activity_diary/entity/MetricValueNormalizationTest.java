package com.example.activity_diary.entity;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.EntryMetricValueGoal;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.EntryTemplateMetricValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricValueNormalizationTest {

    @Test
    void entryMetricValue_roundsAndRejectsRoundedZero() {
        EntryMetric metric = EntryMetric.create(DiaryEntry.builder().build(), dictionaryItem("distance"));
        metric.addValue(dictionaryItem("km"), new BigDecimal("1.234567"));

        EntryMetricValue value = metric.getValues().getFirst();
        assertEquals(new BigDecimal("1.23457"), value.getValue());

        value.changeValue(new BigDecimal("2.345678"));

        assertEquals(new BigDecimal("2.34568"), value.getValue());
        assertThrows(IllegalArgumentException.class, () -> value.changeValue(new BigDecimal("0.000004")));
    }

    @Test
    void entryTemplateMetricValue_roundsAndRejectsRoundedZero() {
        EntryTemplateMetric metric = EntryTemplateMetric.create(DiaryEntryTemplate.builder().build(), dictionaryItem("water"));
        metric.addValue(dictionaryItem("ml"), new BigDecimal("10.123456"));

        EntryTemplateMetricValue value = metric.getValues().iterator().next();
        assertEquals(new BigDecimal("10.12346"), value.getValue());

        value.changeValue(new BigDecimal("11.987654"));

        assertEquals(new BigDecimal("11.98765"), value.getValue());
        assertThrows(IllegalArgumentException.class, () -> value.changeValue(new BigDecimal("0.000004")));
    }

    @Test
    void entryMetricValueGoal_roundsAndRejectsRoundedZero() {
        EntryMetricGoal metricGoal = EntryMetricGoal.create(DiaryEntryGoal.builder().build(), dictionaryItem("steps"));
        metricGoal.addValue(dictionaryItem("count"), new BigDecimal("1000.123456"));

        EntryMetricValueGoal value = metricGoal.getValues().getFirst();
        assertEquals(new BigDecimal("1000.12346"), value.getExpectedValue());

        value.changeExpectedValue(new BigDecimal("999.987654"));

        assertEquals(new BigDecimal("999.98765"), value.getExpectedValue());
        assertThrows(IllegalArgumentException.class, () -> value.changeExpectedValue(new BigDecimal("0.000004")));
    }

    private static DictionaryItem dictionaryItem(String label) {
        return DictionaryItem.builder()
                .label(label)
                .build();
    }
}
