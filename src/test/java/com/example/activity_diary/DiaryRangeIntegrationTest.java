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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = "APP_DB_PATH=./build/diary-range-${random.uuid}.sqlite")
class DiaryRangeIntegrationTest {

    private static final LocalDateTime RANGE_FROM = LocalDateTime.of(2026, 2, 10, 10, 0);
    private static final LocalDateTime RANGE_TO = LocalDateTime.of(2026, 2, 10, 12, 0);

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
    private Tag focusTag;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        tagRepository.deleteAll();

        desktopUser = userRepository.findById(currentUserProvider.getCurrentUserId()).orElseThrow();
        focusTag = tagRepository.save(tag("focus"));
    }

    @Test
    void getByRange_returnsOnlyEntriesThatOverlapRequestedWindow() throws Exception {
        EntryFixture endsAtFrom = saveEntry(
                desktopUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 9, 0)),
                localInstant(LocalDateTime.of(2026, 2, 10, 10, 0))
        );
        EntryFixture overlapsStart = saveEntry(
                desktopUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 9, 30)),
                localInstant(LocalDateTime.of(2026, 2, 10, 10, 30))
        );
        EntryFixture inside = saveEntry(
                desktopUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 10, 15)),
                localInstant(LocalDateTime.of(2026, 2, 10, 11, 0))
        );
        EntryFixture overlapsEnd = saveEntry(
                desktopUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 11, 30)),
                localInstant(LocalDateTime.of(2026, 2, 10, 12, 30))
        );
        EntryFixture startsAtTo = saveEntry(
                desktopUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 12, 0)),
                localInstant(LocalDateTime.of(2026, 2, 10, 13, 0))
        );

        User otherUser = userRepository.save(User.builder()
                .username("other-user-" + UUID.randomUUID())
                .fullName("Other User")
                .enabled(true)
                .role(Role.USER)
                .build());
        saveEntry(
                otherUser,
                localInstant(LocalDateTime.of(2026, 2, 10, 10, 30)),
                localInstant(LocalDateTime.of(2026, 2, 10, 11, 30))
        );

        JsonNode data = responseData(
                mockMvc.perform(get("/api/diary/range")
                                .param("from", RANGE_FROM.toString())
                                .param("to", RANGE_TO.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );

        List<Long> ids = extractIds(data);
        assertThat(ids).containsExactly(
                overlapsStart.id(),
                inside.id(),
                overlapsEnd.id()
        );
        assertThat(ids).doesNotContain(endsAtFrom.id(), startsAtTo.id());
    }

    private EntryFixture saveEntry(User user, Instant whenStarted, Instant whenEnded) {
        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(whenStarted)
                .whenEnded(whenEnded)
                .duration((int) Duration.between(whenStarted, whenEnded).toMinutes())
                .status(EntryStatus.ACTIVE)
                .description("range-entry")
                .tags(new LinkedHashSet<>(List.of(focusTag)))
                .build();

        DiaryEntry saved = diaryRepository.save(entry);
        return new EntryFixture(saved.getId());
    }

    private Tag tag(String name) {
        return Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .createdBy(desktopUser)
                .build();
    }

    private Instant localInstant(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toInstant();
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private List<Long> extractIds(JsonNode data) {
        return data.findValues("id").stream()
                .map(JsonNode::asLong)
                .toList();
    }

    private record EntryFixture(Long id) {
    }
}
