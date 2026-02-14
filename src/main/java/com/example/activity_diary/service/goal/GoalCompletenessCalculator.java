package com.example.activity_diary.service.goal;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.goal.*;

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

        int durationPct = calcDurationPct(
                goal.getExpectedDurationMin(),
                entry.getDuration()
        );

        int metricsPct = calcMetricsPct(goal, entry);

        double total;

        if (goal.getMetricGoals().isEmpty()) {
            total = durationPct;
        } else {
            total = W_DURATION * durationPct +
                    W_METRICS * metricsPct;
        }

        goal.setCompleteness(clamp((int) Math.round(total)));
    }

    private static int calcDurationPct(Integer expected, Integer actual) {
        if (expected == null || expected <= 0) return 0;
        if (actual == null || actual < 0) return 0;

        double pct = ((double) actual / expected) * 100.0;
        return clamp((int) Math.round(pct));
    }

    private static int calcMetricsPct(DiaryEntryGoal goal, DiaryEntry entry) {

        if (goal.getMetricGoals().isEmpty()) return 0;

        Map<Long, EntryMetric> entryMetrics = new HashMap<>();
        for (EntryMetric m : entry.getMetrics()) {
            entryMetrics.put(m.getMetricType().getId(), m);
        }

        double sum = 0;
        int count = 0;

        for (EntryMetricGoal mg : goal.getMetricGoals()) {

            int expectedTotal = mg.getValues().stream()
                    .mapToInt(EntryMetricValueGoal::getExpectedValue)
                    .sum();

            if (expectedTotal <= 0) continue;

            EntryMetric actualMetric =
                    entryMetrics.get(mg.getMetricType().getId());

            int actualTotal = 0;

            if (actualMetric != null) {
                for (EntryMetricValue v : actualMetric.getValues()) {
                    actualTotal += v.getValue();
                }
            }

            double pct = ((double) actualTotal / expectedTotal) * 100.0;
            sum += clamp((int) Math.round(pct));
            count++;
        }

        if (count == 0) return 0;

        return clamp((int) Math.round(sum / count));
    }

    /* ============================================================
       DAY LEVEL
    ============================================================ */

    public static void recalcDayGoal(DayGoal day) {

        if (day.getEntryGoals().isEmpty()) {
            day.setCompleteness(0);
            return;
        }

        int sum = 0;

        for (DiaryEntryGoal g : day.getEntryGoals()) {
            sum += g.getCompleteness();
        }

        day.setCompleteness(clamp(sum / day.getEntryGoals().size()));
    }

    /* ============================================================
       WEEK LEVEL
    ============================================================ */

    public static void recalcWeekGoal(WeekGoal week) {

        if (week.getDays().isEmpty()) {
            week.setCompleteness(0);
            return;
        }

        int sum = 0;

        for (DayGoal d : week.getDays()) {
            sum += d.getCompleteness();
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
