package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartSeriesDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.food.GeneralFood;
import com.example.activity_diary.entity.food.UserFood;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import com.example.activity_diary.repository.food.UserFoodRepository;
import com.example.activity_diary.service.impl.diary.EntryMetricDetailsLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class FoodChartSupportService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String CALORIES_LABEL = "calories";
    private static final String PROTEIN_LABEL = "protein";
    private static final String FAT_LABEL = "fat";
    private static final String CARBS_LABEL = "carbs";
    private static final Set<String> GRAM_UNIT_LABELS = Set.of(
            "g",
            "gram",
            "grams",
            "гр",
            "г",
            "грамм",
            "грамма",
            "граммы"
    );
    private static final Comparator<DiaryEntry> ENTRY_ORDER = Comparator
            .comparing(DiaryEntry::getWhenStarted, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(DiaryEntry::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    private static final Comparator<EntryMetric> METRIC_ORDER = Comparator
            .comparing(
                    (EntryMetric metric) -> metric.getMetricType() == null ? null : metric.getMetricType().getLabel(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            )
            .thenComparing(
                    metric -> metric.getMetricType() == null ? null : metric.getMetricType().getId(),
                    Comparator.nullsLast(Comparator.naturalOrder())
            )
            .thenComparing(EntryMetric::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final DiaryRepository diaryRepository;
    private final UserFoodRepository userFoodRepository;
    private final GeneralFoodRepository generalFoodRepository;
    private final EntryMetricDetailsLoader entryMetricDetailsLoader;

    public FoodChartSupportService(
            DiaryRepository diaryRepository,
            UserFoodRepository userFoodRepository,
            GeneralFoodRepository generalFoodRepository,
            EntryMetricDetailsLoader entryMetricDetailsLoader
    ) {
        this.diaryRepository = diaryRepository;
        this.userFoodRepository = userFoodRepository;
        this.generalFoodRepository = generalFoodRepository;
        this.entryMetricDetailsLoader = entryMetricDetailsLoader;
    }

    public ChartResponseDto buildCaloriesPerDay(ChartType chartType, Long userId, ChartFilterDto filter) {
        List<DiaryEntry> entries = findEntries(userId, filter);
        if (entries.isEmpty()) {
            return emptyResponse(chartType);
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = loadDetailedMetrics(entries);
        Map<Long, FoodProfile> foodProfiles = loadFoodProfiles(userId, metricsByEntryId);
        Map<LocalDate, NutritionTotals> totalsByDay = new TreeMap<>();

        for (DiaryEntry entry : entries) {
            if (entry.getWhenStarted() == null) {
                continue;
            }

            LocalDate day = entry.getWhenStarted().atZone(ZoneOffset.UTC).toLocalDate();
            totalsByDay.merge(day, calculateEntryNutrition(entry.getId(), metricsByEntryId, foodProfiles), NutritionTotals::add);
        }

        List<ChartSeriesDto> series = totalsByDay.values().stream()
                .map(this::toCaloriesSeries)
                .toList();

        return new ChartResponseDto(chartType, series);
    }

    public ChartResponseDto buildCaloriesPerDiary(ChartType chartType, Long userId, ChartFilterDto filter) {
        List<DiaryEntry> entries = findEntries(userId, filter);
        if (entries.isEmpty()) {
            return emptyResponse(chartType);
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = loadDetailedMetrics(entries);
        Map<Long, FoodProfile> foodProfiles = loadFoodProfiles(userId, metricsByEntryId);
        List<ChartSeriesDto> series = entries.stream()
                .map(entry -> toCaloriesSeries(calculateEntryNutrition(entry.getId(), metricsByEntryId, foodProfiles)))
                .toList();

        return new ChartResponseDto(chartType, series);
    }

    public ChartResponseDto buildPfcPerDay(ChartType chartType, Long userId, ChartFilterDto filter) {
        List<DiaryEntry> entries = findEntries(userId, filter);
        if (entries.isEmpty()) {
            return emptyResponse(chartType);
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = loadDetailedMetrics(entries);
        Map<Long, FoodProfile> foodProfiles = loadFoodProfiles(userId, metricsByEntryId);
        Map<LocalDate, NutritionTotals> totalsByDay = new TreeMap<>();

        for (DiaryEntry entry : entries) {
            if (entry.getWhenStarted() == null) {
                continue;
            }

            LocalDate day = entry.getWhenStarted().atZone(ZoneOffset.UTC).toLocalDate();
            totalsByDay.merge(day, calculateEntryNutrition(entry.getId(), metricsByEntryId, foodProfiles), NutritionTotals::add);
        }

        List<ChartSeriesDto> series = totalsByDay.values().stream()
                .map(this::toPfcSeries)
                .toList();

        return new ChartResponseDto(chartType, series);
    }

    public ChartResponseDto buildPfcPerDiary(ChartType chartType, Long userId, ChartFilterDto filter) {
        List<DiaryEntry> entries = findEntries(userId, filter);
        if (entries.isEmpty()) {
            return emptyResponse(chartType);
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = loadDetailedMetrics(entries);
        Map<Long, FoodProfile> foodProfiles = loadFoodProfiles(userId, metricsByEntryId);
        List<ChartSeriesDto> series = entries.stream()
                .map(entry -> toPfcSeries(calculateEntryNutrition(entry.getId(), metricsByEntryId, foodProfiles)))
                .toList();

        return new ChartResponseDto(chartType, series);
    }

    public ChartResponseDto buildPfcPerMetric(ChartType chartType, Long userId, ChartFilterDto filter) {
        List<DiaryEntry> entries = findEntries(userId, filter);
        if (entries.isEmpty()) {
            return emptyResponse(chartType);
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = loadDetailedMetrics(entries);
        Map<Long, FoodProfile> foodProfiles = loadFoodProfiles(userId, metricsByEntryId);
        List<ChartSeriesDto> series = new ArrayList<>();

        for (DiaryEntry entry : entries) {
            for (EntryMetric metric : sortMetrics(metricsByEntryId.get(entry.getId()))) {
                calculateMetricNutrition(metric, foodProfiles)
                        .map(this::toPfcSeries)
                        .ifPresent(series::add);
            }
        }

        return new ChartResponseDto(chartType, series);
    }

    private List<DiaryEntry> findEntries(Long userId, ChartFilterDto filter) {
        return diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(
                        userId,
                        filter.getTagId(),
                        filter.getDateFrom(),
                        filter.getDateTo()
                ).stream()
                .sorted(ENTRY_ORDER)
                .toList();
    }

    private Map<Long, List<EntryMetric>> loadDetailedMetrics(List<DiaryEntry> entries) {
        return entryMetricDetailsLoader.loadForEntries(
                entries.stream().map(DiaryEntry::getId).toList()
        );
    }

    private Map<Long, FoodProfile> loadFoodProfiles(Long userId, Map<Long, List<EntryMetric>> metricsByEntryId) {
        Set<Long> dictionaryItemIds = metricsByEntryId.values().stream()
                .flatMap(List::stream)
                .map(EntryMetric::getMetricType)
                .filter(metricType -> metricType != null && metricType.getId() != null)
                .map(metricType -> metricType.getId())
                .collect(java.util.stream.Collectors.toSet());

        if (dictionaryItemIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, FoodProfile> foodProfiles = new HashMap<>();

        for (GeneralFood food : generalFoodRepository.findAllByDictionaryItemIdIn(dictionaryItemIds)) {
            foodProfiles.put(food.getDictionaryItem().getId(), FoodProfile.fromGeneralFood(food));
        }

        for (UserFood food : userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(userId, dictionaryItemIds)) {
            foodProfiles.put(food.getDictionaryItem().getId(), FoodProfile.fromUserFood(food));
        }

        return foodProfiles;
    }

    private NutritionTotals calculateEntryNutrition(
            Long entryId,
            Map<Long, List<EntryMetric>> metricsByEntryId,
            Map<Long, FoodProfile> foodProfiles
    ) {
        NutritionTotals total = NutritionTotals.zero();

        for (EntryMetric metric : sortMetrics(metricsByEntryId.get(entryId))) {
            total = total.add(calculateMetricNutrition(metric, foodProfiles).orElseGet(NutritionTotals::zero));
        }

        return total;
    }

    private Optional<NutritionTotals> calculateMetricNutrition(
            EntryMetric metric,
            Map<Long, FoodProfile> foodProfiles
    ) {
        if (metric == null || metric.getMetricType() == null || metric.getMetricType().getId() == null) {
            return Optional.empty();
        }

        FoodProfile foodProfile = foodProfiles.get(metric.getMetricType().getId());
        if (foodProfile == null) {
            return Optional.empty();
        }

        BigDecimal grams = extractGrams(metric.getValues());
        if (grams.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        return Optional.of(foodProfile.calculateForGrams(grams));
    }

    private BigDecimal extractGrams(List<EntryMetricValue> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return values.stream()
                .filter(this::isGramValue)
                .map(EntryMetricValue::getValue)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isGramValue(EntryMetricValue value) {
        if (value == null || value.getUnit() == null || value.getUnit().getLabel() == null) {
            return false;
        }

        String normalizedLabel = value.getUnit().getLabel().trim().toLowerCase(Locale.ROOT);
        return GRAM_UNIT_LABELS.contains(normalizedLabel);
    }

    private List<EntryMetric> sortMetrics(Collection<EntryMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return List.of();
        }

        return metrics.stream()
                .sorted(METRIC_ORDER)
                .toList();
    }

    private ChartSeriesDto toCaloriesSeries(NutritionTotals totals) {
        return new ChartSeriesDto(List.of(
                new ChartPointDto(CALORIES_LABEL, totals.getCalories())
        ));
    }

    private ChartSeriesDto toPfcSeries(NutritionTotals totals) {
        return new ChartSeriesDto(List.of(
                new ChartPointDto(PROTEIN_LABEL, totals.getProtein()),
                new ChartPointDto(FAT_LABEL, totals.getFat()),
                new ChartPointDto(CARBS_LABEL, totals.getCarbs())
        ));
    }

    private ChartResponseDto emptyResponse(ChartType chartType) {
        return new ChartResponseDto(chartType, List.of());
    }

    private static final class FoodProfile {
        private final BigDecimal caloriesPerHundredGrams;
        private final BigDecimal proteinPerHundredGrams;
        private final BigDecimal fatPerHundredGrams;
        private final BigDecimal carbsPerHundredGrams;

        private FoodProfile(
                BigDecimal caloriesPerHundredGrams,
                BigDecimal proteinPerHundredGrams,
                BigDecimal fatPerHundredGrams,
                BigDecimal carbsPerHundredGrams
        ) {
            this.caloriesPerHundredGrams = normalize(caloriesPerHundredGrams);
            this.proteinPerHundredGrams = normalize(proteinPerHundredGrams);
            this.fatPerHundredGrams = normalize(fatPerHundredGrams);
            this.carbsPerHundredGrams = normalize(carbsPerHundredGrams);
        }

        private static FoodProfile fromGeneralFood(GeneralFood food) {
            return new FoodProfile(
                    food.getCallories(),
                    food.getProtein(),
                    food.getFat(),
                    food.getCarbs()
            );
        }

        private static FoodProfile fromUserFood(UserFood food) {
            return new FoodProfile(
                    food.getCallories(),
                    food.getProtein(),
                    food.getFat(),
                    food.getCarbs()
            );
        }

        private NutritionTotals calculateForGrams(BigDecimal grams) {
            return new NutritionTotals(
                    caloriesPerHundredGrams.multiply(grams).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP),
                    proteinPerHundredGrams.multiply(grams).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP),
                    fatPerHundredGrams.multiply(grams).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP),
                    carbsPerHundredGrams.multiply(grams).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
            );
        }
    }

    private static final class NutritionTotals {
        private final BigDecimal calories;
        private final BigDecimal protein;
        private final BigDecimal fat;
        private final BigDecimal carbs;

        private NutritionTotals(
                BigDecimal calories,
                BigDecimal protein,
                BigDecimal fat,
                BigDecimal carbs
        ) {
            this.calories = defaultValue(calories);
            this.protein = defaultValue(protein);
            this.fat = defaultValue(fat);
            this.carbs = defaultValue(carbs);
        }

        private static NutritionTotals zero() {
            return new NutritionTotals(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        private NutritionTotals add(NutritionTotals other) {
            return new NutritionTotals(
                    calories.add(other.calories),
                    protein.add(other.protein),
                    fat.add(other.fat),
                    carbs.add(other.carbs)
            );
        }

        private BigDecimal getCalories() {
            return normalize(calories);
        }

        private BigDecimal getProtein() {
            return normalize(protein);
        }

        private BigDecimal getFat() {
            return normalize(fat);
        }

        private BigDecimal getCarbs() {
            return normalize(carbs);
        }
    }

    private static BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal normalize(BigDecimal value) {
        return defaultValue(value).setScale(2, RoundingMode.HALF_UP);
    }
}
