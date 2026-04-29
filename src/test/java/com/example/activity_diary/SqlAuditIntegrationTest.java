package com.example.activity_diary;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.UserTagId;
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
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.example.activity_diary.service.diary.DiaryService;
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
    private EntityManager entityManager;

    private User desktopUser;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
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
        assertThat(result.statements()).isNotEmpty();
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
        assertThat(result.statements()).isNotEmpty();
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
        assertThat(result.statements()).isNotEmpty();
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
        assertThat(result.statements()).isNotEmpty();
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
        assertThat(result.statements()).isNotEmpty();
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

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }

    private record SqlAuditResult(List<String> statements) {

        private void print(String label) {
            long selectCount = statements.stream()
                    .filter(sql -> sql.startsWith("select "))
                    .count();
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
