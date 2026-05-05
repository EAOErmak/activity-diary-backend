package com.example.activity_diary;

import com.example.activity_diary.entity.TagUsageAgg;
import com.example.activity_diary.entity.TagUsageAggId;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.tag.TagUsageAggRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "APP_DB_PATH=./build/admin-tag-update-integration-${random.uuid}.sqlite",
        "app.admin.database.clear.enabled=true"
})
class AdminTagUpdateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Autowired
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private UserTagRepository userTagRepository;

    @Autowired
    private TagUsageAggRepository tagUsageAggRepository;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        userTagRepository.deleteAll();
        tagMetricLinkRepository.deleteAll();
        tagChartTypeLinkRepository.deleteAll();
        tagUsageAggRepository.deleteAll();
        tagRepository.deleteAll();
        dictionaryRepository.deleteAll();
    }

    @Test
    void updateTag_normalizesLeadingHashtagAndReturnsUpdatedDto() throws Exception {
        Tag tag = tagRepository.save(Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build());

        mockMvc.perform(put("/api/admin/tags/{id}", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "#Workout"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.id").value(tag.getId()))
                .andExpect(jsonPath("$.data.name").value("workout"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        Tag updated = tagRepository.findById(tag.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("workout");
        assertThat(updated.getId()).isEqualTo(tag.getId());
    }

    @Test
    void updateTag_duplicateNameReturnsBadRequest() throws Exception {
        Tag sport = tagRepository.save(Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build());
        Tag workout = tagRepository.save(Tag.builder()
                .name("workout")
                .status(TagStatus.APPROVED)
                .build());

        mockMvc.perform(put("/api/admin/tags/{id}", workout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "sport"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tag already exists"))
                .andExpect(jsonPath("$.data").doesNotExist());

        assertThat(tagRepository.findById(sport.getId()).orElseThrow().getName()).isEqualTo("sport");
        assertThat(tagRepository.findById(workout.getId()).orElseThrow().getName()).isEqualTo("workout");
    }

    @Test
    void updateTag_missingIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/tags/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "workout"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tag not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteTag_existingWithoutLinks_deletesTagAndRemovesItFromAdminList() throws Exception {
        Tag tag = saveApprovedTag("delete-no-links");

        mockMvc.perform(delete("/api/admin/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(tagRepository.findById(tag.getId())).isEmpty();

        JsonNode body = readBody(
                mockMvc.perform(get("/api/admin/tags")
                                .param("page", "0")
                                .param("size", "20"))
                        .andExpect(status().isOk())
                        .andReturn()
        );

        assertThat(extractLongField(body.path("data").path("content"), "id"))
                .doesNotContain(tag.getId());
    }

    @Test
    void deleteTag_existingWithMetricLinks_removesLinksAndDeletesTag() throws Exception {
        Tag tag = saveApprovedTag("delete-metric-link");
        DictionaryItem metricName = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label(unique("metric"))
                .active(true)
                .build());
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricName));

        mockMvc.perform(delete("/api/admin/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(tagRepository.findById(tag.getId())).isEmpty();
        assertThat(tagMetricLinkRepository.findByTagId(tag.getId())).isEmpty();
        assertThat(dictionaryRepository.findById(metricName.getId())).isPresent();
    }

    @Test
    void deleteTag_existingWithChartTypeLinks_removesLinksAndDeletesTag() throws Exception {
        Tag tag = saveApprovedTag("delete-chart-link");
        tagChartTypeLinkRepository.save(TagChartTypeLink.create(tag, ChartType.PFC_PER_DAY));

        mockMvc.perform(delete("/api/admin/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(tagRepository.findById(tag.getId())).isEmpty();
        assertThat(tagChartTypeLinkRepository.findByTagId(tag.getId())).isEmpty();
    }

    @Test
    void deleteTag_referencedByDiaryEntry_keepsDiaryEntryAndUnlinksDeletedTag() throws Exception {
        User user = userRepository.save(User.builder()
                .username(unique("user"))
                .enabled(true)
                .role(Role.USER)
                .build());
        Tag deletedTag = saveApprovedTag("delete-entry-tag");
        Tag retainedTag = saveApprovedTag("retain-entry-tag");

        DiaryEntry entry = diaryRepository.save(DiaryEntry.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-04-26T10:00:00Z"))
                .whenEnded(Instant.parse("2026-04-26T11:00:00Z"))
                .duration(60)
                .description("entry with tags")
                .status(EntryStatus.FINISHED)
                .tags(new HashSet<>(Set.of(deletedTag, retainedTag)))
                .build());

        userTagRepository.save(UserTag.create(user, deletedTag));
        tagUsageAggRepository.save(TagUsageAgg.builder()
                .id(new TagUsageAggId(user.getId(), deletedTag.getId(), TagUsageBucket.DAY, LocalDate.of(2026, 4, 26)))
                .usageCount(1)
                .totalDurationMinutes(60)
                .build());

        mockMvc.perform(delete("/api/admin/tags/{id}", deletedTag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        DiaryEntry reloaded = diaryRepository.findGraphByIdAndUser_Id(entry.getId(), user.getId()).orElseThrow();

        assertThat(diaryRepository.findById(entry.getId())).isPresent();
        assertThat(reloaded.getTags())
                .extracting(Tag::getId)
                .contains(retainedTag.getId())
                .doesNotContain(deletedTag.getId());
        assertThat(userTagRepository.findAll()).isEmpty();
        assertThat(tagUsageAggRepository.findAll()).isEmpty();
        assertThat(tagRepository.findById(deletedTag.getId())).isEmpty();
        assertThat(tagRepository.findById(retainedTag.getId())).isPresent();
    }

    @Test
    void deleteTag_missingIdReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/tags/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tag not found"));
    }

    @Test
    void replaceTagMetrics_emptyToNonEmpty_replacesLinks() throws Exception {
        Tag tag = saveApprovedTag("replace-metrics-add");
        DictionaryItem metricA = saveMetricName("distance");
        DictionaryItem metricB = saveMetricName("weight");

        mockMvc.perform(put("/api/admin/tags/{id}/metrics", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricReplaceRequest(metricB.getId(), metricA.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].metricNameId").value(metricA.getId()))
                .andExpect(jsonPath("$.data[1].metricNameId").value(metricB.getId()));

        assertThat(tagMetricLinkRepository.findByTagId(tag.getId()))
                .extracting(link -> link.getMetricName().getId())
                .containsExactly(metricA.getId(), metricB.getId());
    }

    @Test
    void replaceTagMetrics_existingToDifferentList_replacesLinksWithoutDuplicates() throws Exception {
        Tag tag = saveApprovedTag("replace-metrics-swap");
        DictionaryItem metricA = saveMetricName("distance");
        DictionaryItem metricB = saveMetricName("weight");
        DictionaryItem metricC = saveMetricName("calories");
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricA));
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricB));

        mockMvc.perform(put("/api/admin/tags/{id}/metrics", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricReplaceRequest(metricC.getId(), metricC.getId(), metricB.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].metricNameId").value(metricC.getId()))
                .andExpect(jsonPath("$.data[1].metricNameId").value(metricB.getId()));

        assertThat(tagMetricLinkRepository.findByTagId(tag.getId()))
                .extracting(link -> link.getMetricName().getId())
                .containsExactly(metricC.getId(), metricB.getId());
    }

    @Test
    void replaceTagMetrics_withEmptyList_clearsLinks() throws Exception {
        Tag tag = saveApprovedTag("replace-metrics-clear");
        DictionaryItem metricA = saveMetricName("distance");
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricA));

        mockMvc.perform(put("/api/admin/tags/{id}/metrics", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricReplaceRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(tagMetricLinkRepository.findByTagId(tag.getId())).isEmpty();
    }

    @Test
    void replaceTagMetrics_invalidMetricId_returnsBadRequestAndKeepsLinks() throws Exception {
        Tag tag = saveApprovedTag("replace-metrics-invalid-id");
        DictionaryItem metricA = saveMetricName("distance");
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricA));

        mockMvc.perform(put("/api/admin/tags/{id}/metrics", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricReplaceRequest(999999L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Metric name not found"));

        assertThat(tagMetricLinkRepository.findByTagId(tag.getId()))
                .extracting(link -> link.getMetricName().getId())
                .containsExactly(metricA.getId());
    }

    @Test
    void replaceTagMetrics_invalidDictionaryType_returnsBadRequestAndKeepsLinks() throws Exception {
        Tag tag = saveApprovedTag("replace-metrics-invalid-type");
        DictionaryItem metricA = saveMetricName("distance");
        DictionaryItem unit = dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label(unique("kg"))
                .active(true)
                .build());
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricA));

        mockMvc.perform(put("/api/admin/tags/{id}/metrics", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricReplaceRequest(unit.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Metric name must be of type METRIC_NAME"));

        assertThat(tagMetricLinkRepository.findByTagId(tag.getId()))
                .extracting(link -> link.getMetricName().getId())
                .containsExactly(metricA.getId());
    }

    private Tag saveApprovedTag(String baseName) {
        return tagRepository.save(Tag.builder()
                .name(unique(baseName))
                .status(TagStatus.APPROVED)
                .build());
    }

    private DictionaryItem saveMetricName(String baseLabel) {
        return dictionaryRepository.save(DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label(unique(baseLabel))
                .active(true)
                .build());
    }

    private Object metricReplaceRequest(Long... metricNameIds) {
        return java.util.Map.of("metricNameIds", List.of(metricNameIds));
    }

    private String unique(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private List<Long> extractLongField(JsonNode arrayNode, String fieldName) {
        return arrayNode.findValues(fieldName).stream()
                .map(JsonNode::asLong)
                .toList();
    }
}
