package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartSeriesDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.food.GeneralFood;
import com.example.activity_diary.entity.food.UserFood;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import com.example.activity_diary.repository.food.UserFoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodChartStrategiesTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserFoodRepository userFoodRepository;

    @Mock
    private GeneralFoodRepository generalFoodRepository;

    private CaloriesPerDayChartStrategy caloriesPerDayChartStrategy;
    private CaloriesPerDiaryChartStrategy caloriesPerDiaryChartStrategy;
    private CaloriesPerEatingChartStrategy caloriesPerEatingChartStrategy;
    private PfcPerDayChartStrategy pfcPerDayChartStrategy;
    private PfcPerDiaryChartStrategy pfcPerDiaryChartStrategy;
    private PfcPerEatingChartStrategy pfcPerEatingChartStrategy;
    private PfcPerMetricChartStrategy pfcPerMetricChartStrategy;

    @BeforeEach
    void setUp() {
        FoodChartSupportService supportService = new FoodChartSupportService(
                diaryRepository,
                userFoodRepository,
                generalFoodRepository
        );

        caloriesPerDayChartStrategy = new CaloriesPerDayChartStrategy(supportService);
        caloriesPerDiaryChartStrategy = new CaloriesPerDiaryChartStrategy(supportService);
        caloriesPerEatingChartStrategy = new CaloriesPerEatingChartStrategy(supportService);
        pfcPerDayChartStrategy = new PfcPerDayChartStrategy(supportService);
        pfcPerDiaryChartStrategy = new PfcPerDiaryChartStrategy(supportService);
        pfcPerEatingChartStrategy = new PfcPerEatingChartStrategy(supportService);
        pfcPerMetricChartStrategy = new PfcPerMetricChartStrategy(supportService);
    }

    @Test
    void caloriesPerDay_groupsByUtcDayAndUsesUserFoodBeforeGeneralFood() {
        ChartFilterDto filter = filter(ChartType.CALORIES_PER_DAY);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(sampleEntries());
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(
                        generalFood(1L, "Apple", "1.00", "9.99", "9.99", "9.99"),
                        generalFood(2L, "Banana", "4.00", "0.40", "0.50", "0.60")
                ));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of(
                        userFood(11L, 1L, "Apple", "2.00", "0.10", "0.20", "0.30")
                ));

        ChartResponseDto response = caloriesPerDayChartStrategy.calculate(11L, filter);

        assertEquals(ChartType.CALORIES_PER_DAY, response.getChartType());
        assertEquals(2, response.getSeries().size());
        assertSinglePoint(response.getSeries().get(0), "calories", "380.00");
        assertSinglePoint(response.getSeries().get(1), "calories", "120.00");
    }

    @Test
    void caloriesPerDiary_returnsOneSeriesPerEntryAndKeepsZeroWhenFoodIsMissing() {
        ChartFilterDto filter = filter(ChartType.CALORIES_PER_DIARY);
        DiaryEntry first = diaryEntry(101L, "2026-02-01T10:00:00Z",
                metricSpec(1L, "Apple", 100));
        DiaryEntry second = diaryEntry(102L, "2026-02-01T12:00:00Z",
                metricSpec(3L, "Unknown", 25));

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(List.of(first, second));
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(generalFood(1L, "Apple", "2.00", "0.10", "0.20", "0.30")));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of());

        ChartResponseDto response = caloriesPerDiaryChartStrategy.calculate(11L, filter);

        assertEquals(2, response.getSeries().size());
        assertSinglePoint(response.getSeries().get(0), "calories", "200.00");
        assertSinglePoint(response.getSeries().get(1), "calories", "0.00");
    }

    @Test
    void pfcPerDay_returnsThreePointsPerDay() {
        ChartFilterDto filter = filter(ChartType.PFC_PER_DAY);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(sampleEntries());
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(
                        generalFood(1L, "Apple", "1.00", "9.99", "9.99", "9.99"),
                        generalFood(2L, "Banana", "4.00", "0.40", "0.50", "0.60")
                ));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of(
                        userFood(11L, 1L, "Apple", "2.00", "0.10", "0.20", "0.30")
                ));

        ChartResponseDto response = pfcPerDayChartStrategy.calculate(11L, filter);

        assertEquals(2, response.getSeries().size());
        assertPfcSeries(response.getSeries().get(0), "23.00", "40.00", "57.00");
        assertPfcSeries(response.getSeries().get(1), "12.00", "15.00", "18.00");
    }

    @Test
    void pfcPerDiary_returnsThreePointsPerEntry() {
        ChartFilterDto filter = filter(ChartType.PFC_PER_DIARY);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(sampleEntries());
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(
                        generalFood(1L, "Apple", "1.00", "9.99", "9.99", "9.99"),
                        generalFood(2L, "Banana", "4.00", "0.40", "0.50", "0.60")
                ));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of(
                        userFood(11L, 1L, "Apple", "2.00", "0.10", "0.20", "0.30")
                ));

        ChartResponseDto response = pfcPerDiaryChartStrategy.calculate(11L, filter);

        assertEquals(3, response.getSeries().size());
        assertPfcSeries(response.getSeries().get(0), "10.00", "20.00", "30.00");
        assertPfcSeries(response.getSeries().get(1), "13.00", "20.00", "27.00");
        assertPfcSeries(response.getSeries().get(2), "12.00", "15.00", "18.00");
    }

    @Test
    void pfcPerMetric_skipsMetricsWithoutFood() {
        ChartFilterDto filter = filter(ChartType.PFC_PER_METRIC);
        DiaryEntry entry = diaryEntry(201L, "2026-02-01T10:00:00Z",
                metricSpec(1L, "Apple", 100),
                metricSpec(3L, "Unknown", 50),
                metricSpec(2L, "Banana", 20));

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(List.of(entry));
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(
                        generalFood(1L, "Apple", "2.00", "0.10", "0.20", "0.30"),
                        generalFood(2L, "Banana", "4.00", "0.40", "0.50", "0.60")
                ));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of());

        ChartResponseDto response = pfcPerMetricChartStrategy.calculate(11L, filter);

        assertEquals(2, response.getSeries().size());
        assertPfcSeries(response.getSeries().get(0), "10.00", "20.00", "30.00");
        assertPfcSeries(response.getSeries().get(1), "8.00", "10.00", "12.00");
    }

    @Test
    void eatingAliasesReuseDiaryStrategies() {
        ChartFilterDto caloriesFilter = filter(ChartType.CALORIES_PER_EATING);
        ChartFilterDto pfcFilter = filter(ChartType.PFC_PER_EATING);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, caloriesFilter.getDateFrom(), caloriesFilter.getDateTo()))
                .thenReturn(sampleEntries());
        when(generalFoodRepository.findAllByDictionaryItemIdIn(anyCollection()))
                .thenReturn(List.of(
                        generalFood(1L, "Apple", "1.00", "9.99", "9.99", "9.99"),
                        generalFood(2L, "Banana", "4.00", "0.40", "0.50", "0.60")
                ));
        when(userFoodRepository.findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection()))
                .thenReturn(List.of(
                        userFood(11L, 1L, "Apple", "2.00", "0.10", "0.20", "0.30")
                ));

        ChartResponseDto caloriesResponse = caloriesPerEatingChartStrategy.calculate(11L, caloriesFilter);
        ChartResponseDto pfcResponse = pfcPerEatingChartStrategy.calculate(11L, pfcFilter);

        assertEquals(ChartType.CALORIES_PER_EATING, caloriesResponse.getChartType());
        assertEquals(3, caloriesResponse.getSeries().size());
        assertSinglePoint(caloriesResponse.getSeries().get(1), "calories", "180.00");

        assertEquals(ChartType.PFC_PER_EATING, pfcResponse.getChartType());
        assertEquals(3, pfcResponse.getSeries().size());
        assertPfcSeries(pfcResponse.getSeries().get(1), "13.00", "20.00", "27.00");
    }

    @Test
    void strategies_returnEmptySeriesWhenNoEntriesFound() {
        ChartFilterDto filter = filter(ChartType.CALORIES_PER_DAY);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(List.of());

        ChartResponseDto response = caloriesPerDayChartStrategy.calculate(11L, filter);

        assertEquals(ChartType.CALORIES_PER_DAY, response.getChartType());
        assertEquals(List.of(), response.getSeries());
        verify(generalFoodRepository, never()).findAllByDictionaryItemIdIn(anyCollection());
        verify(userFoodRepository, never()).findAllByUserIdAndDictionaryItemIdIn(eq(11L), anyCollection());
    }

    private static ChartFilterDto filter(ChartType chartType) {
        return new ChartFilterDto(
                7L,
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                chartType
        );
    }

    private static List<DiaryEntry> sampleEntries() {
        return List.of(
                diaryEntry(101L, "2026-02-01T10:00:00Z",
                        metricSpec(1L, "Apple", 100)),
                diaryEntry(102L, "2026-02-01T12:00:00Z",
                        metricSpec(1L, "Apple", 50),
                        metricSpec(2L, "Banana", 20)),
                diaryEntry(103L, "2026-02-02T01:00:00Z",
                        metricSpec(2L, "Banana", 30))
        );
    }

    private static DiaryEntry diaryEntry(Long id, String whenStarted, MetricSpec... metrics) {
        DiaryEntry entry = DiaryEntry.builder()
                .whenStarted(Instant.parse(whenStarted))
                .build();
        entry.setId(id);

        for (MetricSpec spec : metrics) {
            EntryMetric metric = EntryMetric.create(entry, dictionaryItem(spec.dictionaryItemId(), spec.label()));
            for (int i = 0; i < spec.amounts().length; i++) {
                metric.addValue(dictionaryItem(1000L + spec.dictionaryItemId() + i, "g-" + i), spec.amounts()[i]);
            }
            entry.addMetric(metric);
        }

        return entry;
    }

    private static MetricSpec metricSpec(Long dictionaryItemId, String label, int... amounts) {
        return new MetricSpec(dictionaryItemId, label, amounts);
    }

    private static GeneralFood generalFood(
            Long dictionaryItemId,
            String label,
            String calories,
            String protein,
            String fat,
            String carbs
    ) {
        return GeneralFood.builder()
                .dictionaryItem(dictionaryItem(dictionaryItemId, label))
                .callories(new BigDecimal(calories))
                .protein(new BigDecimal(protein))
                .fat(new BigDecimal(fat))
                .carbs(new BigDecimal(carbs))
                .build();
    }

    private static UserFood userFood(
            Long userId,
            Long dictionaryItemId,
            String label,
            String calories,
            String protein,
            String fat,
            String carbs
    ) {
        User user = User.builder().username("tester-" + userId).build();
        user.setId(userId);

        return UserFood.builder()
                .user(user)
                .dictionaryItem(dictionaryItem(dictionaryItemId, label))
                .callories(new BigDecimal(calories))
                .protein(new BigDecimal(protein))
                .fat(new BigDecimal(fat))
                .carbs(new BigDecimal(carbs))
                .build();
    }

    private static DictionaryItem dictionaryItem(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .label(label)
                .build();
        item.setId(id);
        return item;
    }

    private static void assertSinglePoint(ChartSeriesDto series, String label, String value) {
        assertEquals(1, series.getPoints().size());
        ChartPointDto point = series.getPoints().get(0);
        assertEquals(label, point.getLabel());
        assertEquals(new BigDecimal(value), point.getValue());
    }

    private static void assertPfcSeries(ChartSeriesDto series, String protein, String fat, String carbs) {
        assertEquals(3, series.getPoints().size());
        assertPoint(series.getPoints().get(0), "protein", protein);
        assertPoint(series.getPoints().get(1), "fat", fat);
        assertPoint(series.getPoints().get(2), "carbs", carbs);
    }

    private static void assertPoint(ChartPointDto point, String expectedLabel, String expectedValue) {
        assertEquals(expectedLabel, point.getLabel());
        assertEquals(new BigDecimal(expectedValue), point.getValue());
    }

    private record MetricSpec(Long dictionaryItemId, String label, int[] amounts) {
    }
}
