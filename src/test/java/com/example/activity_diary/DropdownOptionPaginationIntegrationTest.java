package com.example.activity_diary;

import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.dict.MetricNameUnitLink;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.MetricNameUnitLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "APP_DB_PATH=./build/dropdown-option-pagination-integration-${random.uuid}.sqlite",
        "app.admin.database.clear.enabled=true"
})
class DropdownOptionPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Autowired
    private MetricNameUnitLinkRepository metricNameUnitLinkRepository;

    @BeforeEach
    void setUp() {
        tagMetricLinkRepository.deleteAll();
        metricNameUnitLinkRepository.deleteAll();
        tagRepository.deleteAll();
        dictionaryRepository.deleteAll();
    }

    @Test
    void getTagMetrics_returnsDistinctPaginatedSearchableItems() throws Exception {
        Tag firstTag = saveTag("first-tag");
        Tag secondTag = saveTag("second-tag");

        DictionaryItem alpha = saveDictionary(DictionaryType.METRIC_NAME, "Alpha");
        DictionaryItem alphaSecond = saveDictionary(DictionaryType.METRIC_NAME, "alpha");
        DictionaryItem beta = saveDictionary(DictionaryType.METRIC_NAME, "Beta");
        DictionaryItem delta = saveDictionary(DictionaryType.METRIC_NAME, "delta");
        DictionaryItem gamma = saveDictionary(DictionaryType.METRIC_NAME, "Gamma");
        DictionaryItem omega = saveDictionary(DictionaryType.METRIC_NAME, "omega");
        DictionaryItem zeta = saveDictionary(DictionaryType.METRIC_NAME, "Zeta");

        link(firstTag, alpha);
        link(firstTag, alphaSecond);
        link(firstTag, beta);
        link(firstTag, delta);
        link(secondTag, beta);
        link(secondTag, gamma);
        link(secondTag, omega);
        link(secondTag, zeta);

        JsonNode firstPage = readBody(
                mockMvc.perform(get("/api/tags/metrics")
                                .param("tagIds", String.valueOf(firstTag.getId()), String.valueOf(secondTag.getId()))
                                .param("page", "0")
                                .param("limit", "3"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.items.length()").value(3))
                        .andExpect(jsonPath("$.data.page").value(0))
                        .andExpect(jsonPath("$.data.limit").value(3))
                        .andExpect(jsonPath("$.data.totalElements").value(7))
                        .andExpect(jsonPath("$.data.totalPages").value(3))
                        .andExpect(jsonPath("$.data.hasNext").value(true))
                        .andExpect(jsonPath("$.data.hasPrevious").value(false))
                        .andReturn()
        );

        JsonNode secondPage = readBody(
                mockMvc.perform(get("/api/tags/metrics")
                                .param("tagIds", String.valueOf(firstTag.getId()), String.valueOf(secondTag.getId()))
                                .param("page", "1")
                                .param("limit", "3"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(3))
                        .andExpect(jsonPath("$.data.page").value(1))
                        .andExpect(jsonPath("$.data.hasNext").value(true))
                        .andExpect(jsonPath("$.data.hasPrevious").value(true))
                        .andReturn()
        );

        JsonNode searchPage = readBody(
                        mockMvc.perform(get("/api/tags/metrics")
                                .param("tagIds", String.valueOf(firstTag.getId()), String.valueOf(secondTag.getId()))
                                .param("page", "0")
                                .param("limit", "6")
                                .param("q", " mm "))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(1))
                        .andExpect(jsonPath("$.data.totalElements").value(1))
                        .andExpect(jsonPath("$.data.totalPages").value(1))
                        .andReturn()
        );

        assertThat(extractLongField(firstPage.path("data").path("items"), "id"))
                .containsExactly(alpha.getId(), alphaSecond.getId(), beta.getId());
        assertThat(extractLongField(secondPage.path("data").path("items"), "id"))
                .containsExactly(delta.getId(), gamma.getId(), omega.getId());
        assertThat(extractLongField(searchPage.path("data").path("items"), "id"))
                .containsExactly(gamma.getId());
    }

    @Test
    void getMetricUnits_returnsPaginatedSearchableItems() throws Exception {
        DictionaryItem metricName = saveDictionary(DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem alpha = saveDictionary(DictionaryType.METRIC_UNIT, "Alpha");
        DictionaryItem alphaSecond = saveDictionary(DictionaryType.METRIC_UNIT, "alpha");
        DictionaryItem beta = saveDictionary(DictionaryType.METRIC_UNIT, "Beta");
        DictionaryItem delta = saveDictionary(DictionaryType.METRIC_UNIT, "delta");

        link(metricName, alpha);
        link(metricName, alphaSecond);
        link(metricName, beta);
        link(metricName, delta);

        JsonNode firstPage = readBody(
                mockMvc.perform(get("/api/dictionary/metric-names/{metricNameId}/units", metricName.getId())
                                .param("page", "0")
                                .param("limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(4))
                        .andExpect(jsonPath("$.data.totalPages").value(2))
                        .andExpect(jsonPath("$.data.hasNext").value(true))
                        .andExpect(jsonPath("$.data.hasPrevious").value(false))
                        .andReturn()
        );

        JsonNode secondPage = readBody(
                mockMvc.perform(get("/api/dictionary/metric-names/{metricNameId}/units", metricName.getId())
                                .param("page", "1")
                                .param("limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.hasNext").value(false))
                        .andExpect(jsonPath("$.data.hasPrevious").value(true))
                        .andReturn()
        );

        JsonNode searchPage = readBody(
                mockMvc.perform(get("/api/dictionary/metric-names/{metricNameId}/units", metricName.getId())
                                .param("page", "0")
                                .param("limit", "6")
                                .param("q", "ta"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(2))
                        .andExpect(jsonPath("$.data.totalPages").value(1))
                        .andReturn()
        );

        assertThat(extractLongField(firstPage.path("data").path("items"), "id"))
                .containsExactly(alpha.getId(), alphaSecond.getId());
        assertThat(extractLongField(secondPage.path("data").path("items"), "id"))
                .containsExactly(beta.getId(), delta.getId());
        assertThat(extractLongField(searchPage.path("data").path("items"), "id"))
                .containsExactly(beta.getId(), delta.getId());
    }

    @Test
    void getMetricUnits_missingMetricName_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/dictionary/metric-names/999999/units")
                        .param("page", "0")
                        .param("limit", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(0))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.limit").value(6))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.hasPrevious").value(false));
    }

    private Tag saveTag(String name) {
        return tagRepository.save(Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .build());
    }

    private DictionaryItem saveDictionary(DictionaryType type, String label) {
        return dictionaryRepository.save(DictionaryItem.builder()
                .type(type)
                .label(label)
                .active(true)
                .build());
    }

    private void link(Tag tag, DictionaryItem metricName) {
        tagMetricLinkRepository.save(TagMetricLink.create(tag, metricName));
    }

    private void link(DictionaryItem metricName, DictionaryItem metricUnit) {
        metricNameUnitLinkRepository.save(MetricNameUnitLink.create(metricName, metricUnit));
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
