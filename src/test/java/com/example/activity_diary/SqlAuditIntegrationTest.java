package com.example.activity_diary;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.UserTagId;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import com.example.activity_diary.repository.food.UserFoodRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import com.example.activity_diary.service.diary.TagService;
import com.example.activity_diary.service.diary.DiaryService;
import com.example.activity_diary.service.goal.GoalCalendarService;
import com.example.activity_diary.support.SqlCaptureStatementInspector;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "APP_DB_PATH=./build/sql-audit-${random.uuid}.sqlite",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.example.activity_diary.support.SqlCaptureStatementInspector"
})
class SqlAuditIntegrationTest {

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
    private UserTagRepository userTagRepository;

    @Autowired
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private GoalCalendarService goalCalendarService;

    @Autowired
    private TagService tagService;

    @Autowired
    private GeneralFoodRepository generalFoodRepository;

    @Autowired
    private UserFoodRepository userFoodRepository;

    @Autowired
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Autowired
    private DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    @Autowired
    private DayTemplateRepository dayTemplateRepository;

    @Autowired
    private WeekTemplateRepository weekTemplateRepository;

    @Autowired
    private DiaryEntryGoalRepository diaryEntryGoalRepository;

    @Autowired
    private DayGoalRepository dayGoalRepository;

    @Autowired
    private WeekGoalRepository weekGoalRepository;

    @Autowired
    private EntityManager entityManager;

    private User desktopUser;

    @BeforeEach
    void setUp() {
        diaryEntryGoalRepository.deleteAll();
        dayGoalRepository.deleteAll();
        weekGoalRepository.deleteAll();
        weekTemplateRepository.deleteAll();
        dayTemplateRepository.deleteAll();
        diaryEntryTemplateRepository.deleteAll();
        userFoodRepository.deleteAll();
        generalFoodRepository.deleteAll();
        diaryRepository.deleteAll();
        tagMetricLinkRepository.deleteAll();
        tagChartTypeLinkRepository.deleteAll();
        userTagRepository.deleteAll();
        tagRepository.deleteAll();
        dictionaryRepository.deleteAll();
        SqlCaptureStatementInspector.clear();

        desktopUser = userRepository.findById(currentUserProvider.getCurrentUserId()).orElseThrow();
    }

    @Test
    void auditDiaryMineFiltered() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        saveEntry("planned", EntryStatus.PLANNED, instant("2026-02-10T12:00:00Z"), 60, focusTag);
        saveEntry("active", EntryStatus.ACTIVE, instant("2026-02-10T09:30:00Z"), 60, focusTag);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/diary/mine")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk()));

        result.print("GET /api/diary/mine?status=ACTIVE");
        result.assertAtMost(2, 2);
    }

    @Test
    void auditDiaryRange() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        saveEntry("inside", EntryStatus.ACTIVE, localInstant("2026-02-10T10:15:00"), 60, focusTag);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/diary/range")
                        .param("from", "2026-02-10T10:00:00")
                        .param("to", "2026-02-10T12:00:00"))
                .andExpect(status().isOk()));

        result.print("GET /api/diary/range");
        result.assertAtMost(2, 2);
    }

    @Test
    void auditDiaryGetById() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        DictionaryItem metricType = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label("duration")
                .build());
        DictionaryItem unit = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label("minutes")
                .build());

        DiaryEntry entry = saveEntry("inside", EntryStatus.ACTIVE, instant("2026-02-10T09:30:00Z"), 60, focusTag);
        EntryMetric metric = EntryMetric.create(entry, metricType);
        metric.addValue(unit, BigDecimal.TEN);
        entry.addMetric(metric);
        DiaryEntry saved = diaryRepository.save(entry);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/diary/{id}", saved.getId()))
                .andExpect(status().isOk()));

        result.print("GET /api/diary/{id}");
        result.assertAtMost(3, 3);
    }

    @Test
    void auditDiaryCreate() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);

        Map<String, Object> body = Map.of(
                "whenStarted", "2026-02-10T09:00:00Z",
                "whenEnded", "2026-02-10T10:00:00Z",
                "mood", 4,
                "description", "#focus deep work"
        );

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(post("/api/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isCreated()));

        result.print("POST /api/diary");
        result.assertAtMost(11, 4);
    }

    @Test
    void auditDiaryServiceCreateWithoutManagedUser() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);
        Long userId = desktopUser.getId();
        entityManager.clear();

        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(instant("2026-02-10T09:00:00Z"));
        dto.setWhenEnded(instant("2026-02-10T10:00:00Z"));
        dto.setMood((short) 4);
        dto.setDescription("#focus deep work");

        SqlAuditResult result = captureRequest(() -> diaryService.create(dto, userId));

        result.print("DiaryService.create(userId)");
        result.assertAtMost(10, 3);
    }

    @Test
    void auditTrainingRawChart() throws Exception {
        Tag trainingTag = saveVisibleTag("pullups");
        tagChartTypeLinkRepository.save(TagChartTypeLink.create(trainingTag, ChartType.TRAINING_RAW));

        DictionaryItem metricType = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label("reps")
                .build());
        DictionaryItem unit = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label("count")
                .build());

        DiaryEntry entry = saveEntry("training", EntryStatus.FINISHED, instant("2026-04-12T08:00:00Z"), 30, trainingTag);
        EntryMetric metric = EntryMetric.create(entry, metricType);
        metric.addValue(unit, BigDecimal.TEN);
        entry.addMetric(metric);
        diaryRepository.save(entry);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/analytics/charts")
                        .param("tagId", trainingTag.getId().toString())
                        .param("chartType", ChartType.TRAINING_RAW.name())
                        .param("dateFrom", "2026-04-11T12:12:55.356Z")
                        .param("dateTo", "2026-05-11T12:12:55.356Z"))
                .andExpect(status().isOk()));

        result.print("GET /api/analytics/charts?chartType=TRAINING_RAW");
        result.assertAtMost(5, 5);
    }

    @Test
    void auditGeneralFoodSearch() throws Exception {
        saveGeneralFood("apple");
        saveGeneralFood("banana");

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/general-foods")
                        .param("q", "app"))
                .andExpect(status().isOk()));

        result.print("GET /api/general-foods?q=app");
        result.assertAtMost(1, 1);
    }

    @Test
    void auditUserFoodSearch() throws Exception {
        saveUserFood("apple");
        saveUserFood("banana");

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/user-foods")
                        .param("q", "app"))
                .andExpect(status().isOk()));

        result.print("GET /api/user-foods?q=app");
        result.assertAtMost(2, 2);
    }

    @Test
    void auditTagAutocompleteService() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);

        SqlAuditResult result = captureRequest(() -> tagService.getVisibleTags(
                desktopUser.getId(),
                desktopUser.getRole(),
                "fo"
        ));

        result.print("TagService.getVisibleTags(q=fo)");
        result.assertAtMost(1, 1);
    }

    @Test
    void auditEntryTemplateGetById() throws Exception {
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        var template = saveEntryTemplate(
                "focus-template",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/entry-templates/{id}", template.getId()))
                .andExpect(status().isOk()));

        result.print("GET /api/entry-templates/{id}");
        result.assertAtMost(3, 3);
    }

    @Test
    void auditDayTemplateList() throws Exception {
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        var entryTemplate = saveEntryTemplate(
                "focus-template",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );
        saveDayTemplate("focus-day", entryTemplate);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/day-templates"))
                .andExpect(status().isOk()));

        result.print("GET /api/day-templates");
        result.assertAtMost(3, 3);
    }

    @Test
    void auditWeekTemplateList() throws Exception {
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        var entryTemplate = saveEntryTemplate(
                "focus-template",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );
        var dayTemplate = saveDayTemplate("focus-day", entryTemplate);
        saveWeekTemplate("focus-week", dayTemplate);

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/week-templates"))
                .andExpect(status().isOk()));

        result.print("GET /api/week-templates");
        result.assertAtMost(3, 3);
    }

    @Test
    void auditCreateEntryGoalFromTemplate() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        var template = saveEntryTemplate(
                "goal-template",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );

        Map<String, Object> body = Map.of(
                "templateId", template.getId(),
                "targetDate", "2026-02-10"
        );

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(post("/api/goal/entry/drop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk()));

        result.print("POST /api/goal/entry/drop");
        result.assertAtMost(20, 10);
    }

    @Test
    void auditDayGoalDetail() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        tagMetricLinkRepository.save(TagMetricLink.create(focusTag, metricType));

        var first = saveEntryTemplate(
                "goal-template-1",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );
        var second = saveEntryTemplate(
                "goal-template-2",
                "#focus second session",
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                metricType,
                unit,
                BigDecimal.valueOf(20)
        );
        var dayTemplate = saveDayTemplate("goal-day", first, second);
        DayGoalDetailDto created = goalCalendarService.createDayGoal(
                desktopUser.getId(),
                dayTemplate.getId(),
                LocalDate.of(2026, 2, 10)
        );

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/goal/day/{id}", created.getId()))
                .andExpect(status().isOk()));

        result.print("GET /api/goal/day/{id}");
        result.assertAtMost(3, 3);
    }

    @Test
    void auditConfirmDayGoal() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        tagMetricLinkRepository.save(TagMetricLink.create(focusTag, metricType));

        var first = saveEntryTemplate(
                "goal-template-1",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );
        var second = saveEntryTemplate(
                "goal-template-2",
                "#focus second session",
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                metricType,
                unit,
                BigDecimal.valueOf(20)
        );
        var dayTemplate = saveDayTemplate("goal-day", first, second);
        long createdDayGoalId = createDayGoalViaApi(dayTemplate.getId(), LocalDate.of(2026, 2, 10));

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(post("/api/goal/day/{id}/confirm", createdDayGoalId))
                .andExpect(status().isOk()));

        result.print("POST /api/goal/day/{id}/confirm");
        result.assertAtMost(48, 18);
    }

    @Test
    void auditDayGoalSummaryRange() throws Exception {
        Tag focusTag = saveVisibleTag("focus");
        linkUserToTag(focusTag);
        DictionaryItem metricType = saveMetricName("duration");
        DictionaryItem unit = saveMetricUnit("minutes");
        var template = saveEntryTemplate(
                "goal-template",
                "#focus deep work",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                metricType,
                unit,
                BigDecimal.TEN
        );
        var dayTemplate = saveDayTemplate("goal-day", template);

        goalCalendarService.createDayGoal(desktopUser.getId(), dayTemplate.getId(), LocalDate.of(2026, 2, 9));
        goalCalendarService.createDayGoal(desktopUser.getId(), dayTemplate.getId(), LocalDate.of(2026, 2, 10));
        goalCalendarService.createDayGoal(desktopUser.getId(), dayTemplate.getId(), LocalDate.of(2026, 2, 11));

        SqlAuditResult result = captureRequest(() -> mockMvc.perform(get("/api/goal/day/summary")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-28"))
                .andExpect(status().isOk()));

        result.print("GET /api/goal/day/summary");
        result.assertAtMost(2, 2);
    }

    private SqlAuditResult captureRequest(ThrowingOperation operation) throws Exception {
        SqlCaptureStatementInspector.clear();
        operation.run();
        return new SqlAuditResult(SqlCaptureStatementInspector.snapshot());
    }

    private Tag saveVisibleTag(String name) {
        return tagRepository.save(Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .createdBy(desktopUser)
                .build());
    }

    private DictionaryItem saveMetricName(String label) {
        return saveDictionaryItem(DictionaryType.METRIC_NAME, label);
    }

    private DictionaryItem saveMetricUnit(String label) {
        return saveDictionaryItem(DictionaryType.METRIC_UNIT, label);
    }

    private DictionaryItem saveDictionaryItem(DictionaryType type, String label) {
        return dictionaryRepository.save(DictionaryItem.builder()
                .type(type)
                .label(label)
                .build());
    }

    private com.example.activity_diary.entity.food.GeneralFood saveGeneralFood(String label) {
        DictionaryItem dictionaryItem = saveMetricName("food-" + label);
        return generalFoodRepository.save(com.example.activity_diary.entity.food.GeneralFood.builder()
                .dictionaryItem(dictionaryItem)
                .protein(BigDecimal.TEN)
                .fat(BigDecimal.ONE)
                .carbs(BigDecimal.valueOf(20))
                .callories(BigDecimal.valueOf(100))
                .build());
    }

    private com.example.activity_diary.entity.food.UserFood saveUserFood(String label) {
        DictionaryItem dictionaryItem = saveMetricName("food-" + label);
        return userFoodRepository.save(com.example.activity_diary.entity.food.UserFood.builder()
                .user(desktopUser)
                .dictionaryItem(dictionaryItem)
                .protein(BigDecimal.TEN)
                .fat(BigDecimal.ONE)
                .carbs(BigDecimal.valueOf(20))
                .callories(BigDecimal.valueOf(100))
                .build());
    }

    private com.example.activity_diary.entity.template.DiaryEntryTemplate saveEntryTemplate(
            String name,
            String description,
            LocalTime timeStart,
            LocalTime timeEnd,
            DictionaryItem metricType,
            DictionaryItem unit,
            BigDecimal value
    ) {
        var template = com.example.activity_diary.entity.template.DiaryEntryTemplate.create(
                desktopUser,
                name,
                (short) 4,
                description,
                timeStart,
                timeEnd
        );
        var metric = com.example.activity_diary.entity.template.EntryTemplateMetric.create(template, metricType);
        metric.addValue(unit, value);
        template.addMetric(metric);
        return diaryEntryTemplateRepository.save(template);
    }

    private com.example.activity_diary.entity.template.DayTemplate saveDayTemplate(
            String name,
            com.example.activity_diary.entity.template.DiaryEntryTemplate... entryTemplates
    ) {
        var template = com.example.activity_diary.entity.template.DayTemplate.builder()
                .user(desktopUser)
                .name(name)
                .build();

        for (int index = 0; index < entryTemplates.length; index++) {
            template.getItems().add(com.example.activity_diary.entity.template.TemplateEntryItem.builder()
                    .dayTemplate(template)
                    .entryTemplate(entryTemplates[index])
                    .position(index + 1)
                    .build());
        }

        return dayTemplateRepository.save(template);
    }

    private com.example.activity_diary.entity.template.WeekTemplate saveWeekTemplate(
            String name,
            com.example.activity_diary.entity.template.DayTemplate... dayTemplates
    ) {
        var template = com.example.activity_diary.entity.template.WeekTemplate.builder()
                .user(desktopUser)
                .name(name)
                .build();

        for (int index = 0; index < dayTemplates.length; index++) {
            template.getItems().add(com.example.activity_diary.entity.template.TemplateDayItem.builder()
                    .weekTemplate(template)
                    .dayTemplate(dayTemplates[index])
                    .dayOfWeek(index + 1)
                    .build());
        }

        return weekTemplateRepository.save(template);
    }

    private long createDayGoalViaApi(Long templateId, LocalDate targetDate) throws Exception {
        Map<String, Object> body = Map.of(
                "templateId", templateId,
                "targetDate", targetDate.toString()
        );

        String responseBody = mockMvc.perform(post("/api/goal/day/drop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();
    }

    private void linkUserToTag(Tag tag) {
        userTagRepository.save(UserTag.builder()
                .id(new UserTagId(desktopUser.getId(), tag.getId()))
                .user(desktopUser)
                .tag(tag)
                .build());
    }

    private DiaryEntry saveEntry(
            String description,
            EntryStatus status,
            Instant whenStarted,
            int durationMinutes,
            Tag... tags
    ) {
        Instant whenEnded = whenStarted.plus(Duration.ofMinutes(durationMinutes));
        return diaryRepository.save(DiaryEntry.builder()
                .user(desktopUser)
                .whenStarted(whenStarted)
                .whenEnded(whenEnded)
                .duration(durationMinutes)
                .status(status)
                .description(description)
                .tags(new LinkedHashSet<>(List.of(tags)))
                .build());
    }

    private Instant instant(String value) {
        return Instant.parse(value);
    }

    private Instant localInstant(String value) {
        return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }

    private record SqlAuditResult(List<String> statements) {

        private void assertAtMost(long totalStatements, long selectStatements) {
            assertThat(totalStatements())
                    .withFailMessage("Expected at most %s SQL statements but got %s%n%s",
                            totalStatements, totalStatements(), statements)
                    .isLessThanOrEqualTo(totalStatements);
            assertThat(selectStatements())
                    .withFailMessage("Expected at most %s SELECT statements but got %s%n%s",
                            selectStatements, selectStatements(), statements)
                    .isLessThanOrEqualTo(selectStatements);
        }

        private long totalStatements() {
            return statements.size();
        }

        private long selectStatements() {
            return statements.stream()
                    .filter(sql -> sql.startsWith("select "))
                    .count();
        }

        private void print(String label) {
            long selectCount = selectStatements();
            Map<String, Long> grouped = statements.stream()
                    .collect(Collectors.groupingBy(sql -> sql, java.util.LinkedHashMap::new, Collectors.counting()));

            System.out.println();
            System.out.println("=== SQL AUDIT: " + label + " ===");
            System.out.println("totalStatements=" + statements.size() + ", selectStatements=" + selectCount);
            grouped.forEach((sql, count) -> System.out.println(count + " x " + sql));
            System.out.println("=== END SQL AUDIT ===");
            System.out.println();
        }
    }
}
