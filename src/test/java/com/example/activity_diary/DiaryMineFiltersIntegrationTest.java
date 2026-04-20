package com.example.activity_diary;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = "APP_DB_PATH=./build/diary-mine-filters-${random.uuid}.sqlite")
class DiaryMineFiltersIntegrationTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-02-10T10:00:00Z");

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

    private User desktopUser;
    private EntryFixture plannedEntry;
    private EntryFixture activeEntry;
    private EntryFixture overdueEntry;
    private EntryFixture finishedEntry;
    private EntryFixture failedEntry;
    private EntryFixture deletedEntry;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        tagRepository.deleteAll();

        desktopUser = userRepository.findById(currentUserProvider.getCurrentUserId()).orElseThrow();

        Tag focusTag = tagRepository.save(tag("#focus"));
        Tag fitnessTag = tagRepository.save(tag("#fitness"));
        Tag readingTag = tagRepository.save(tag("#reading"));

        plannedEntry = saveEntry(
                "planned",
                EntryStatus.PLANNED,
                FIXED_NOW.plus(Duration.ofHours(2)),
                FIXED_NOW.plus(Duration.ofHours(3)),
                focusTag
        );
        activeEntry = saveEntry(
                "active",
                EntryStatus.ACTIVE,
                FIXED_NOW.minus(Duration.ofMinutes(30)),
                FIXED_NOW.plus(Duration.ofMinutes(30)),
                focusTag,
                fitnessTag
        );
        overdueEntry = saveEntry(
                "overdue",
                EntryStatus.OVERDUE,
                FIXED_NOW.minus(Duration.ofHours(2)),
                FIXED_NOW.minus(Duration.ofHours(1)),
                readingTag
        );
        finishedEntry = saveEntry(
                "finished",
                EntryStatus.FINISHED,
                FIXED_NOW.minus(Duration.ofHours(4)),
                FIXED_NOW.minus(Duration.ofHours(3)),
                fitnessTag,
                readingTag
        );
        failedEntry = saveEntry(
                "failed",
                EntryStatus.FAILED,
                FIXED_NOW.minus(Duration.ofHours(5)),
                FIXED_NOW.minus(Duration.ofHours(4)),
                focusTag,
                readingTag
        );
        deletedEntry = saveEntry(
                "deleted",
                EntryStatus.DELETED,
                FIXED_NOW.minus(Duration.ofHours(6)),
                FIXED_NOW.minus(Duration.ofHours(5)),
                focusTag
        );

        User otherUser = userRepository.save(User.builder()
                .username("other-user-" + UUID.randomUUID())
                .fullName("Other User")
                .enabled(true)
                .role(Role.USER)
                .build());

        DiaryEntry otherUsersActiveEntry = DiaryEntry.builder()
                .user(otherUser)
                .whenStarted(FIXED_NOW.minus(Duration.ofMinutes(15)))
                .whenEnded(FIXED_NOW.plus(Duration.ofMinutes(45)))
                .duration(60)
                .status(EntryStatus.ACTIVE)
                .description("other")
                .tags(new LinkedHashSet<>(List.of(focusTag)))
                .build();
        diaryRepository.save(otherUsersActiveEntry);
    }

    @Test
    void myEntries_withoutUiStatus_returnsAllNonDeletedEntries() throws Exception {
        JsonNode content = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(content)).containsExactly(
                plannedEntry.id(),
                activeEntry.id(),
                overdueEntry.id(),
                finishedEntry.id(),
                failedEntry.id()
        );
        assertThat(extractIds(content)).doesNotContain(deletedEntry.id());
    }

    @Test
    void myEntries_withUiStatus_returnsOnlyMatchingEntries() throws Exception {
        JsonNode activeContent = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString())
                                .param("uiStatus", "ACTIVE"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(activeContent)).containsExactly(activeEntry.id());
        assertThat(extractFirstTags(activeContent)).containsExactly("#focus");

        JsonNode finishedContent = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString())
                                .param("uiStatus", "FINISHED"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(finishedContent)).containsExactly(finishedEntry.id());
        assertThat(extractFirstTags(finishedContent)).containsExactly("#fitness");
    }

    @Test
    void myEntries_withTagFiltersOnly_requiresAllTags() throws Exception {
        JsonNode content = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString())
                                .param("tags", "#focus", "#fitness"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(content)).containsExactly(activeEntry.id());
        assertThat(extractFirstTags(content)).containsExactly("#focus");
    }

    @Test
    void myEntries_withDateFiltersOnly_usesWhenStartedRange() throws Exception {
        JsonNode content = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString())
                                .param("from", FIXED_NOW.minus(Duration.ofHours(3)).toString())
                                .param("to", FIXED_NOW.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(content)).containsExactly(activeEntry.id(), overdueEntry.id());
    }

    @Test
    void myEntries_withCombinedFilters_intersectsStatusTagsAndDate() throws Exception {
        JsonNode content = responseContent(
                mockMvc.perform(get("/api/diary/mine")
                                .param("now", FIXED_NOW.toString())
                                .param("uiStatus", "ACTIVE")
                                .param("tags", "#focus", "#fitness")
                                .param("from", FIXED_NOW.minus(Duration.ofHours(1)).toString())
                                .param("to", FIXED_NOW.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        assertThat(extractIds(content)).containsExactly(activeEntry.id());
    }

    private EntryFixture saveEntry(
            String description,
            EntryStatus status,
            Instant whenStarted,
            Instant whenEnded,
            Tag... tags
    ) {
        DiaryEntry entry = DiaryEntry.builder()
                .user(desktopUser)
                .whenStarted(whenStarted)
                .whenEnded(whenEnded)
                .duration((int) Duration.between(whenStarted, whenEnded).toMinutes())
                .status(status)
                .description(description)
                .tags(new LinkedHashSet<>(List.of(tags)))
                .build();

        DiaryEntry saved = diaryRepository.save(entry);
        return new EntryFixture(saved.getId(), description);
    }

    private Tag tag(String name) {
        return Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .createdBy(desktopUser)
                .build();
    }

    private JsonNode responseContent(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("content");
    }

    private List<Long> extractIds(JsonNode content) {
        return content.findValues("id").stream()
                .map(JsonNode::asLong)
                .toList();
    }

    private List<String> extractFirstTags(JsonNode content) {
        return content.findValues("firstTag").stream()
                .map(JsonNode::asText)
                .toList();
    }

    private record EntryFixture(Long id, String description) {
    }
}
