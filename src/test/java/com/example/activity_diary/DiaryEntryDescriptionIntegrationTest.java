package com.example.activity_diary;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
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

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = "APP_DB_PATH=./build/diary-entry-description-${random.uuid}.sqlite")
class DiaryEntryDescriptionIntegrationTest {

    private static final String SINGLE_TAG_DESCRIPTION =
            "#\u043f\u043e\u0434\u0442\u044f\u0433\u0438\u0432\u0430\u043d\u0438\u044f";
    private static final String MULTI_TAG_DESCRIPTION =
            "\u0441\u0435\u0433\u043e\u0434\u043d\u044f #\u0442\u0440\u0435\u043d\u0438\u0440\u043e\u0432\u043a\u0430 \u043d\u0430 #\u043f\u043e\u0434\u0442\u044f\u0433\u0438\u0432\u0430\u043d\u0438\u044f";
    private static final String PLAIN_DESCRIPTION =
            "\u043e\u0431\u044b\u0447\u043d\u043e\u0435 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u0435";
    private static final String FIRST_TAG = "\u0442\u0440\u0435\u043d\u0438\u0440\u043e\u0432\u043a\u0430";
    private static final String SECOND_TAG = "\u043f\u043e\u0434\u0442\u044f\u0433\u0438\u0432\u0430\u043d\u0438\u044f";
    private static final Instant STARTED_AT = Instant.parse("2026-02-10T10:00:00Z");
    private static final Instant ENDED_AT = Instant.parse("2026-02-10T10:10:00Z");

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
    private TagUsageAggRepository tagUsageAggRepository;

    private User desktopUser;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        userTagRepository.deleteAll();
        tagUsageAggRepository.deleteAll();
        tagRepository.deleteAll();

        desktopUser = userRepository.findById(currentUserProvider.getCurrentUserId()).orElseThrow();
    }

    @Test
    void getById_returnsStoredDescriptionWithLeadingHash() throws Exception {
        Tag tag = saveApprovedTag(SECOND_TAG);
        DiaryEntry entry = saveEntry(SINGLE_TAG_DESCRIPTION, tag);

        mockMvc.perform(get("/api/diary/{id}", entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(entry.getId()))
                .andExpect(jsonPath("$.data.description").value(SINGLE_TAG_DESCRIPTION));
    }

    @Test
    void getById_returnsPlainDescriptionUnchanged() throws Exception {
        DiaryEntry entry = saveEntry(PLAIN_DESCRIPTION);

        mockMvc.perform(get("/api/diary/{id}", entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(entry.getId()))
                .andExpect(jsonPath("$.data.description").value(PLAIN_DESCRIPTION));
    }

    @Test
    void create_returnsOriginalDescriptionAndStoresCanonicalTagName() throws Exception {
        JsonNode data = readData(
                mockMvc.perform(post("/api/diary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto(SINGLE_TAG_DESCRIPTION))))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.description").value(SINGLE_TAG_DESCRIPTION))
                        .andReturn()
        );

        DiaryEntry saved = diaryRepository.findGraphByIdAndUser_Id(data.path("id").asLong(), desktopUser.getId())
                .orElseThrow();

        assertThat(saved.getDescription()).isEqualTo(SINGLE_TAG_DESCRIPTION);
        assertThat(saved.getTags())
                .extracting(Tag::getName)
                .containsExactly(SECOND_TAG);
        assertThat(tagRepository.findByName(SECOND_TAG)).isPresent();
        assertThat(tagRepository.findByName(SINGLE_TAG_DESCRIPTION)).isEmpty();
    }

    @Test
    void create_withMultipleTags_returnsOriginalDescriptionAndResolvesCanonicalTags() throws Exception {
        JsonNode data = readData(
                mockMvc.perform(post("/api/diary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto(MULTI_TAG_DESCRIPTION))))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.description").value(MULTI_TAG_DESCRIPTION))
                        .andReturn()
        );

        DiaryEntry saved = diaryRepository.findGraphByIdAndUser_Id(data.path("id").asLong(), desktopUser.getId())
                .orElseThrow();

        assertThat(saved.getDescription()).isEqualTo(MULTI_TAG_DESCRIPTION);
        assertThat(saved.getTags())
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder(FIRST_TAG, SECOND_TAG);
        assertThat(tagRepository.findByName(FIRST_TAG)).isPresent();
        assertThat(tagRepository.findByName(SECOND_TAG)).isPresent();
    }

    private DiaryEntryCreateDto createDto(String description) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(STARTED_AT);
        dto.setWhenEnded(ENDED_AT);
        dto.setMood((short) 3);
        dto.setDescription(description);
        return dto;
    }

    private DiaryEntry saveEntry(String description, Tag... tags) {
        return diaryRepository.save(DiaryEntry.builder()
                .user(desktopUser)
                .whenStarted(STARTED_AT)
                .whenEnded(ENDED_AT)
                .duration((int) Duration.between(STARTED_AT, ENDED_AT).toMinutes())
                .status(EntryStatus.FINISHED)
                .description(description)
                .tags(new LinkedHashSet<>(Set.of(tags)))
                .build());
    }

    private Tag saveApprovedTag(String name) {
        return tagRepository.save(Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .createdBy(desktopUser)
                .build());
    }

    private JsonNode readData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
