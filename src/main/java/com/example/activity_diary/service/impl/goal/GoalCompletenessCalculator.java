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

    // Р’РµСЃР° (РјРѕР¶РµС€СЊ РјРµРЅСЏС‚СЊ)
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
            // РµСЃР»Рё РјРµС‚СЂРёРє-С†РµР»РµР№ РЅРµС‚, СЃС‡РёС‚Р°РµРј С‚РѕР»СЊРєРѕ duration
            total = durationPct;
        } else if (goal.getExpectedDurationMin() == null || goal.getExpectedDurationMin() <= 0) {
            // РµСЃР»Рё duration РІ goal РЅРµ Р·Р°РґР°РЅ, СЃС‡РёС‚Р°РµРј С‚РѕР»СЊРєРѕ РјРµС‚СЂРёРєРё
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
     * Metrics% СЃС‡РёС‚Р°РµРј СЃС‚СЂРѕРіРѕ РїРѕ РѕР¶РёРґР°РµРјС‹Рј unitвЂ™Р°Рј РёР· goal:
     *
     * - Р“СЂСѓРїРїРёСЂСѓРµРј actual РїРѕ (metricTypeId -> (unitId -> actualValue))
     * - Р”Р»СЏ РєР°Р¶РґРѕР№ РјРµС‚СЂРёРєРё goal:
     *     РѕР¶РёРґР°РµРјС‹Рµ unitвЂ™С‹ = mg.values
     *     actual Р±РµСЂС‘Рј С‚РѕР»СЊРєРѕ РїРѕ СЌС‚РёРј unitвЂ™Р°Рј (РµСЃР»Рё РЅРµС‚ вЂ” 0)
     * - РС‚РѕРіРѕРІС‹Р№ РїСЂРѕС†РµРЅС‚: РІР·РІРµС€РµРЅРЅРѕ РїРѕ expectedValue (С‡С‚РѕР±С‹ Р±РѕР»СЊС€РёРµ expected РёРјРµР»Рё Р±РѕР»СЊС€РёР№ РІРµСЃ)
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

                // РІРµСЃ = expected (РІР·РІРµС€РµРЅРЅРѕРµ СЃСЂРµРґРЅРµРµ)
                totalExpected += expected;
                weightedSumPct += (double) expected * pctClamped;
            }
        }

        if (totalExpected <= 0) return 0;

        int res = (int) Math.round(weightedSumPct / totalExpected);
        return clamp(res);
    }

    /**
     * РЎС‚СЂРѕРёРј actual Р·РЅР°С‡РµРЅРёСЏ:
     * metricTypeId -> unitId -> sum(value)
     *
     * Р•СЃР»Рё РІ entry РµСЃС‚СЊ РЅРµСЃРєРѕР»СЊРєРѕ РѕРґРёРЅР°РєРѕРІС‹С… unit (РїРѕ РёРґРµРµ Сѓ С‚РµР±СЏ unique РЅР° unit РІ EntryMetricValue),
     * РЅРѕ СЃСѓРјРјРёСЂРѕРІР°РЅРёРµ РІСЃС‘ СЂР°РІРЅРѕ Р±РµР·РѕРїР°СЃРЅРѕ.
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
