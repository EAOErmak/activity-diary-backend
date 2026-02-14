package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.goal.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class GoalCompletenessCalculator {

    private GoalCompletenessCalculator() {}

    private static final int MAX = 200;

    // Веса (можешь менять)
    private static final double W_DURATION = 0.5;
    private static final double W_METRICS = 0.5;

    /* ============================================================
       ENTRY LEVEL
    ============================================================ */

    public static void recalcEntryGoal(DiaryEntryGoal goal, DiaryEntry entry) {

        int durationPct = calcDurationPct(goal.getExpectedDurationMin(), entry);
        int metricsPct = calcMetricsPct(goal, entry);

        double total;

        if (goal.getMetricGoals() == null || goal.getMetricGoals().isEmpty()) {
            // если метрик-целей нет, считаем только duration
            total = durationPct;
        } else if (goal.getExpectedDurationMin() == null || goal.getExpectedDurationMin() <= 0) {
            // если duration в goal не задан, считаем только метрики
            total = metricsPct;
        } else {
            total = W_DURATION * durationPct + W_METRICS * metricsPct;
        }

        goal.setCompleteness(clamp((int) Math.round(total)));
    }

    /**
     * Duration% = actualMinutes / expectedMinutes * 100 (clamp 0..MAX)
     */
    private static int calcDurationPct(Integer expectedMin, DiaryEntry entry) {
        if (expectedMin == null || expectedMin <= 0) return 0;

        Integer actualMin = calcActualMinutes(entry.getWhenStarted(), entry.getWhenEnded());
        if (actualMin == null || actualMin < 0) return 0;

        double pct = ((double) actualMin / expectedMin) * 100.0;
        return clamp((int) Math.round(pct));
    }

    private static Integer calcActualMinutes(Instant started, Instant ended) {
        if (started == null || ended == null) return null;
        if (!ended.isAfter(started)) return null;
        return (int) Duration.between(started, ended).toMinutes();
    }

    /**
     * Metrics% считаем строго по ожидаемым unit’ам из goal:
     *
     * - Группируем actual по (metricTypeId -> (unitId -> actualValue))
     * - Для каждой метрики goal:
     *     ожидаемые unit’ы = mg.values
     *     actual берём только по этим unit’ам (если нет — 0)
     * - Итоговый процент: взвешенно по expectedValue (чтобы большие expected имели больший вес)
     */
    private static int calcMetricsPct(DiaryEntryGoal goal, DiaryEntry entry) {
        if (goal.getMetricGoals() == null || goal.getMetricGoals().isEmpty()) return 0;

        Map<Long, Map<Long, Integer>> actual = buildActualMap(entry);

        long totalExpected = 0;
        double weightedSumPct = 0.0;

        for (EntryMetricGoal mg : goal.getMetricGoals()) {
            if (mg.getMetricType() == null) continue;

            long typeId = mg.getMetricType().getId();
            Map<Long, Integer> actualUnits = actual.getOrDefault(typeId, Map.of());

            if (mg.getValues() == null || mg.getValues().isEmpty()) continue;

            for (EntryMetricValueGoal vg : mg.getValues()) {
                if (vg.getUnit() == null) continue;

                int expected = safePos(vg.getExpectedValue());
                if (expected <= 0) continue;

                long unitId = vg.getUnit().getId();
                int actualVal = safePos(actualUnits.get(unitId));

                double pct = ((double) actualVal / expected) * 100.0;
                int pctClamped = clamp((int) Math.round(pct));

                // вес = expected (взвешенное среднее)
                totalExpected += expected;
                weightedSumPct += (double) expected * pctClamped;
            }
        }

        if (totalExpected <= 0) return 0;

        int res = (int) Math.round(weightedSumPct / totalExpected);
        return clamp(res);
    }

    /**
     * Строим actual значения:
     * metricTypeId -> unitId -> sum(value)
     *
     * Если в entry есть несколько одинаковых unit (по идее у тебя unique на unit в EntryMetricValue),
     * но суммирование всё равно безопасно.
     */
    private static Map<Long, Map<Long, Integer>> buildActualMap(DiaryEntry entry) {
        Map<Long, Map<Long, Integer>> res = new HashMap<>();
        if (entry.getMetrics() == null) return res;

        for (EntryMetric m : entry.getMetrics()) {
            if (m.getMetricType() == null) continue;
            Long typeId = m.getMetricType().getId();

            Map<Long, Integer> unitMap = res.computeIfAbsent(typeId, k -> new HashMap<>());

            if (m.getValues() == null) continue;
            for (EntryMetricValue v : m.getValues()) {
                if (v.getUnit() == null) continue;
                Long unitId = v.getUnit().getId();

                int add = safePos(v.getValue());
                unitMap.merge(unitId, add, Integer::sum);
            }
        }

        return res;
    }

    private static int safePos(Integer v) {
        return v == null ? 0 : Math.max(0, v);
    }

    /* ============================================================
       DAY LEVEL
    ============================================================ */

    public static void recalcDayGoal(DayGoal day) {
        if (day.getEntryGoals() == null || day.getEntryGoals().isEmpty()) {
            day.setCompleteness(0);
            return;
        }

        int sum = 0;
        for (DiaryEntryGoal g : day.getEntryGoals()) {
            sum += safePos(g.getCompleteness());
        }

        day.setCompleteness(clamp(sum / day.getEntryGoals().size()));
    }

    /* ============================================================
       WEEK LEVEL
    ============================================================ */

    public static void recalcWeekGoal(WeekGoal week) {
        if (week.getDays() == null || week.getDays().isEmpty()) {
            week.setCompleteness(0);
            return;
        }

        int sum = 0;
        for (DayGoal d : week.getDays()) {
            sum += safePos(d.getCompleteness());
        }

        week.setCompleteness(clamp(sum / week.getDays().size()));
    }

    /* ============================================================ */

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > MAX) return MAX;
        return v;
    }
}
