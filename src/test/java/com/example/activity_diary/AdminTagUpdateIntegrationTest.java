package com.example.activity_diary;

import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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
    private TagRepository tagRepository;

    @Autowired
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Autowired
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @BeforeEach
    void setUp() {
        tagMetricLinkRepository.deleteAll();
        tagChartTypeLinkRepository.deleteAll();
        tagRepository.deleteAll();
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
}
