package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.EntryMetricValueGoal;
import com.example.activity_diary.entity.goal.WeekGoal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class GoalCompletenessCalculator {

    private static final int MAX = 200;
    private static final int DIVISION_SCALE = 10;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal W_DURATION = new BigDecimal("0.5");
    private static final BigDecimal W_METRICS = new BigDecimal("0.5");

    private GoalCompletenessCalculator() {
    }

    public static void recalcEntryGoal(DiaryEntryGoal goal, DiaryEntry entry) {
        int durationPct = calcDurationPct(goal.getExpectedDurationMin(), entry);
        int metricsPct = calcMetricsPct(goal, entry);

        BigDecimal total;

        if (goal.getMetricGoals() == null || goal.getMetricGoals().isEmpty()) {
            total = BigDecimal.valueOf(durationPct);
        } else if (goal.getExpectedDurationMin() == null || goal.getExpectedDurationMin() <= 0) {
            total = BigDecimal.valueOf(metricsPct);
        } else {
            total = W_DURATION.multiply(BigDecimal.valueOf(durationPct))
                    .add(W_METRICS.multiply(BigDecimal.valueOf(metricsPct)));
        }

        goal.setCompleteness(clamp(total.setScale(0, RoundingMode.HALF_UP).intValue()));
    }

    private static int calcDurationPct(Integer expectedMin, DiaryEntry entry) {
        if (expectedMin == null || expectedMin <= 0) {
            return 0;
        }

        Integer actualMin = calcActualMinutes(entry.getWhenStarted(), entry.getWhenEnded());
        if (actualMin == null || actualMin < 0) {
            return 0;
        }

        BigDecimal pct = BigDecimal.valueOf(actualMin)
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(expectedMin), DIVISION_SCALE, RoundingMode.HALF_UP);

        return clamp(pct.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    private static Integer calcActualMinutes(Instant started, Instant ended) {
        if (started == null || ended == null) {
            return null;
        }
        if (!ended.isAfter(started)) {
            return null;
        }
        return (int) Duration.between(started, ended).toMinutes();
    }

    private static int calcMetricsPct(DiaryEntryGoal goal, DiaryEntry entry) {
        if (goal.getMetricGoals() == null || goal.getMetricGoals().isEmpty()) {
            return 0;
        }

        Map<Long, Map<Long, BigDecimal>> actual = buildActualMap(entry);

        BigDecimal totalExpected = ZERO;
        BigDecimal weightedSumPct = ZERO;

        for (EntryMetricGoal mg : goal.getMetricGoals()) {
            if (mg.getMetricType() == null) {
                continue;
            }

            long typeId = mg.getMetricType().getId();
            Map<Long, BigDecimal> actualUnits = actual.getOrDefault(typeId, Map.of());

            if (mg.getValues() == null || mg.getValues().isEmpty()) {
                continue;
            }

            for (EntryMetricValueGoal vg : mg.getValues()) {
                if (vg.getUnit() == null) {
                    continue;
                }

                BigDecimal expected = safePositive(vg.getExpectedValue());
                if (expected.signum() <= 0) {
                    continue;
                }

                long unitId = vg.getUnit().getId();
                BigDecimal actualVal = safePositive(actualUnits.get(unitId));

                BigDecimal pct = actualVal.multiply(HUNDRED)
                        .divide(expected, DIVISION_SCALE, RoundingMode.HALF_UP);
                int pctClamped = clamp(pct.setScale(0, RoundingMode.HALF_UP).intValue());

                totalExpected = totalExpected.add(expected);
                weightedSumPct = weightedSumPct.add(expected.multiply(BigDecimal.valueOf(pctClamped)));
            }
        }

        if (totalExpected.signum() <= 0) {
            return 0;
        }

        int result = weightedSumPct
                .divide(totalExpected, DIVISION_SCALE, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        return clamp(result);
    }

    private static Map<Long, Map<Long, BigDecimal>> buildActualMap(DiaryEntry entry) {
        Map<Long, Map<Long, BigDecimal>> result = new HashMap<>();
        if (entry.getMetrics() == null) {
            return result;
        }

        for (EntryMetric metric : entry.getMetrics()) {
            if (metric.getMetricType() == null) {
                continue;
            }

            Long typeId = metric.getMetricType().getId();
            Map<Long, BigDecimal> unitMap = result.computeIfAbsent(typeId, k -> new HashMap<>());

            if (metric.getValues() == null) {
                continue;
            }

            for (EntryMetricValue value : metric.getValues()) {
                if (value.getUnit() == null) {
                    continue;
                }

                Long unitId = value.getUnit().getId();
                BigDecimal add = safePositive(value.getValue());
                unitMap.merge(unitId, add, BigDecimal::add);
            }
        }

        return result;
    }

    private static BigDecimal safePositive(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return ZERO;
        }
        return value;
    }

    public static void recalcDayGoal(DayGoal day) {
        if (day.getEntryGoals() == null || day.getEntryGoals().isEmpty()) {
            day.setCompleteness(0);
            return;
        }

        int sum = 0;
        for (DiaryEntryGoal goal : day.getEntryGoals()) {
            sum += safePos(goal.getCompleteness());
        }

        day.setCompleteness(clamp(sum / day.getEntryGoals().size()));
    }

    public static void recalcWeekGoal(WeekGoal week) {
        if (week.getDays() == null || week.getDays().isEmpty()) {
            week.setCompleteness(0);
            return;
        }

        int sum = 0;
        for (DayGoal day : week.getDays()) {
            sum += safePos(day.getCompleteness());
        }

        week.setCompleteness(clamp(sum / week.getDays().size()));
    }

    private static int safePos(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > MAX) {
            return MAX;
        }
        return value;
    }
}
