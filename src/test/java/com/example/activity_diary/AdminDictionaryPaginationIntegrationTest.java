package com.example.activity_diary;

import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.repository.diary.DictionaryRepository;
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
        "APP_DB_PATH=./build/admin-dictionary-pagination-integration-${random.uuid}.sqlite",
        "app.admin.database.clear.enabled=true"
})
class AdminDictionaryPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @BeforeEach
    void setUp() {
        dictionaryRepository.deleteAll();
    }

    @Test
    void getByType_returnsPaginatedMetricNames() throws Exception {
        DictionaryItem alpha = save(DictionaryType.METRIC_NAME, "Alpha");
        DictionaryItem beta = save(DictionaryType.METRIC_NAME, "beta");
        save(DictionaryType.METRIC_NAME, "Delta");
        DictionaryItem alphaSecond = save(DictionaryType.METRIC_NAME, "alpha");
        save(DictionaryType.METRIC_NAME, "Gamma");
        save(DictionaryType.METRIC_UNIT, "kg");

        JsonNode body = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                                .param("page", "0")
                                .param("limit", "3"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(3))
                        .andExpect(jsonPath("$.data.page").value(0))
                        .andExpect(jsonPath("$.data.limit").value(3))
                        .andExpect(jsonPath("$.data.totalElements").value(5))
                        .andExpect(jsonPath("$.data.totalPages").value(2))
                        .andExpect(jsonPath("$.data.hasNext").value(true))
                        .andExpect(jsonPath("$.data.hasPrevious").value(false))
                        .andReturn()
        );

        assertThat(extractLongField(body.path("data").path("items"), "id"))
                .containsExactly(alpha.getId(), alphaSecond.getId(), beta.getId());
    }

    @Test
    void getByType_switchingPagesReturnsDifferentSlices() throws Exception {
        DictionaryItem alpha = save(DictionaryType.METRIC_NAME, "Alpha");
        DictionaryItem beta = save(DictionaryType.METRIC_NAME, "beta");
        DictionaryItem delta = save(DictionaryType.METRIC_NAME, "Delta");
        DictionaryItem gamma = save(DictionaryType.METRIC_NAME, "Gamma");

        JsonNode firstPage = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                                .param("page", "0")
                                .param("limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.page").value(0))
                        .andExpect(jsonPath("$.data.limit").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(4))
                        .andExpect(jsonPath("$.data.totalPages").value(2))
                        .andExpect(jsonPath("$.data.hasNext").value(true))
                        .andExpect(jsonPath("$.data.hasPrevious").value(false))
                        .andReturn()
        );

        JsonNode secondPage = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                                .param("page", "1")
                                .param("limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.page").value(1))
                        .andExpect(jsonPath("$.data.limit").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(4))
                        .andExpect(jsonPath("$.data.totalPages").value(2))
                        .andExpect(jsonPath("$.data.hasNext").value(false))
                        .andExpect(jsonPath("$.data.hasPrevious").value(true))
                        .andReturn()
        );

        assertThat(extractLongField(firstPage.path("data").path("items"), "id"))
                .containsExactly(alpha.getId(), beta.getId());
        assertThat(extractLongField(secondPage.path("data").path("items"), "id"))
                .containsExactly(delta.getId(), gamma.getId());
    }

    @Test
    void getByType_searchesCaseInsensitiveByLabel() throws Exception {
        DictionaryItem lowercase = save(DictionaryType.METRIC_NAME, "test");
        DictionaryItem titlecase = save(DictionaryType.METRIC_NAME, "Test Bench");
        save(DictionaryType.METRIC_NAME, "Deadlift");

        JsonNode lowerCaseSearch = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                                .param("page", "0")
                                .param("limit", "20")
                                .param("q", "test"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(2))
                        .andExpect(jsonPath("$.data.totalPages").value(1))
                        .andReturn()
        );

        JsonNode upperCaseSearch = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                                .param("page", "0")
                                .param("limit", "20")
                                .param("q", "TEST"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(2))
                        .andExpect(jsonPath("$.data.totalPages").value(1))
                        .andReturn()
        );

        assertThat(extractLongField(lowerCaseSearch.path("data").path("items"), "id"))
                .containsExactly(lowercase.getId(), titlecase.getId());
        assertThat(extractLongField(upperCaseSearch.path("data").path("items"), "id"))
                .containsExactly(lowercase.getId(), titlecase.getId());
    }

    @Test
    void getByType_blankQueryBehavesLikeNoFilter() throws Exception {
        save(DictionaryType.METRIC_NAME, "Alpha");
        save(DictionaryType.METRIC_NAME, "Beta");

        mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                        .param("page", "0")
                        .param("limit", "20")
                        .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getByType_omittedQueryBehavesLikeNoFilter() throws Exception {
        save(DictionaryType.METRIC_NAME, "Alpha");
        save(DictionaryType.METRIC_NAME, "Beta");

        mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                        .param("page", "0")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getByType_whitespaceQueryBehavesLikeNoFilter() throws Exception {
        save(DictionaryType.METRIC_NAME, "Alpha");
        save(DictionaryType.METRIC_NAME, "Beta");

        mockMvc.perform(get("/api/admin/dict/METRIC_NAME")
                        .param("page", "0")
                        .param("limit", "20")
                        .param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getByType_returnsPaginatedMetricUnits() throws Exception {
        save(DictionaryType.METRIC_UNIT, "kg");
        save(DictionaryType.METRIC_UNIT, "gram");
        save(DictionaryType.METRIC_UNIT, "ml");
        save(DictionaryType.METRIC_NAME, "Protein");

        JsonNode body = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_UNIT")
                                .param("page", "0")
                                .param("limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.totalElements").value(3))
                        .andExpect(jsonPath("$.data.totalPages").value(2))
                        .andReturn()
        );

        assertThat(extractTextField(body.path("data").path("items"), "label"))
                .containsExactly("gram", "kg");
    }

    private DictionaryItem save(DictionaryType type, String label) {
        return dictionaryRepository.save(DictionaryItem.builder()
                .type(type)
                .label(label)
                .active(true)
                .build());
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private List<Long> extractLongField(JsonNode arrayNode, String fieldName) {
        return arrayNode.findValues(fieldName).stream()
                .map(JsonNode::asLong)
                .toList();
    }

    private List<String> extractTextField(JsonNode arrayNode, String fieldName) {
        return arrayNode.findValues(fieldName).stream()
                .map(JsonNode::asText)
                .toList();
    }
}
