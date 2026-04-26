package com.example.activity_diary;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = "APP_DB_PATH=./build/analytics-chart-date-range-${random.uuid}.sqlite")
class AnalyticsChartDateRangeIntegrationTest {

    private static final Instant DATE_FROM = Instant.parse("2026-04-11T12:12:55.356Z");
    private static final Instant DATE_TO = Instant.parse("2026-05-11T12:12:55.356Z");
    private static final Instant OUTSIDE_RANGE = Instant.parse("2026-04-03T08:00:00Z");
    private static final Instant INSIDE_RANGE = Instant.parse("2026-04-12T08:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    private User desktopUser;
    private Tag trainingTag;
    private DictionaryItem metricType;
    private DictionaryItem unit;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        tagChartTypeLinkRepository.deleteAll();
        tagRepository.deleteAll();
        dictionaryRepository.deleteAll();

        desktopUser = userRepository.findById(currentUserProvider.getCurrentUserId()).orElseThrow();
        trainingTag = tagRepository.save(Tag.builder()
                .name("pullups")
                .status(TagStatus.APPROVED)
                .createdBy(desktopUser)
                .build());

        List.of(
                ChartType.TRAINING_COMPUTED,
                ChartType.TRAINING_METRICS,
                ChartType.TRAINING_RAW,
                ChartType.DURATION_PER_ENTRY
        ).forEach(chartType -> tagChartTypeLinkRepository.save(TagChartTypeLink.create(trainingTag, chartType)));

        metricType = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label("reps")
                .build());
        unit = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label("count")
                .build());
    }

    @ParameterizedTest
    @EnumSource(value = ChartType.class, names = {"TRAINING_COMPUTED", "TRAINING_METRICS"})
    void trainingCharts_returnEmptyWhenMatchingTagEntryIsOutsideDateRange(ChartType chartType) throws Exception {
        saveEntry(OUTSIDE_RANGE, BigDecimal.valueOf(10));

        JsonNode data = requestChart(chartType);

        assertThat(data.path("series").isArray()).isTrue();
        assertThat(data.path("series").size()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ChartType.class, names = {"TRAINING_COMPUTED", "TRAINING_METRICS"})
    void trainingCharts_returnDataWhenMatchingTagEntryIsInsideDateRange(ChartType chartType) throws Exception {
        saveEntry(INSIDE_RANGE, BigDecimal.valueOf(10));

        JsonNode data = requestChart(chartType);

        assertThat(data.path("series").size()).isEqualTo(1);
        assertThat(data.path("series").get(0).path("points").size()).isGreaterThan(0);
    }

    @ParameterizedTest
    @EnumSource(value = ChartType.class, names = {"TRAINING_RAW", "DURATION_PER_ENTRY"})
    void existingEntryCharts_stillReturnOnlyEntriesInsideDateRange(ChartType chartType) throws Exception {
        saveEntry(OUTSIDE_RANGE, BigDecimal.valueOf(5));
        saveEntry(INSIDE_RANGE, BigDecimal.valueOf(10));

        JsonNode data = requestChart(chartType);

        assertThat(data.path("series").size()).isEqualTo(1);
        assertThat(data.path("series").get(0).path("points").size()).isEqualTo(1);
    }

    private JsonNode requestChart(ChartType chartType) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/analytics/charts")
                        .param("tagId", trainingTag.getId().toString())
                        .param("chartType", chartType.name())
                        .param("dateFrom", DATE_FROM.toString())
                        .param("dateTo", DATE_TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chartType").value(chartType.name()))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private DiaryEntry saveEntry(Instant whenStarted, BigDecimal value) {
        Instant whenEnded = whenStarted.plus(Duration.ofMinutes(30));
        DiaryEntry entry = DiaryEntry.builder()
                .user(desktopUser)
                .whenStarted(whenStarted)
                .whenEnded(whenEnded)
                .duration((int) Duration.between(whenStarted, whenEnded).toMinutes())
                .status(EntryStatus.FINISHED)
                .description("training")
                .tags(new LinkedHashSet<>(Set.of(trainingTag)))
                .build();

        EntryMetric metric = EntryMetric.create(entry, metricType);
        metric.addValue(unit, value);
        entry.addMetric(metric);

        return diaryRepository.save(entry);
    }
}
