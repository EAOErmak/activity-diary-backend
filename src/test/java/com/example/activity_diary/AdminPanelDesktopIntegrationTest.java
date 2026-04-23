package com.example.activity_diary;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "APP_DB_PATH=./build/admin-panel-desktop-integration-${random.uuid}.sqlite",
        "app.admin.database.clear.enabled=true"
})
class AdminPanelDesktopIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private GeneralFoodRepository generalFoodRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Test
    void desktopProfileSupportsAdminPanelFlowWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        JsonNode tableTypes = readBody(
                mockMvc.perform(get("/api/admin/database/table-types"))
                        .andExpect(status().isOk())
                        .andReturn()
        );
        List<String> tableTypeValues = toTextList(tableTypes.path("data"));
        assertThat(tableTypeValues)
                .contains("dictionary_item", "users", "user_accounts", "tag", "general_food");

        JsonNode createdUser = readData(
                mockMvc.perform(post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "local-user",
                                          "password": "password123",
                                          "role": "USER"
                                        }
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );
        long createdUserId = createdUser.path("id").asLong();
        assertThat(createdUser.path("username").asText()).isEqualTo("local-user");
        assertThat(createdUser.path("fullName").asText()).isEmpty();
        assertThat(userAccountRepository.existsByProviderAndProviderId(ProviderType.LOCAL, "local-user")).isTrue();

        JsonNode users = readBody(
                mockMvc.perform(get("/api/admin/users"))
                        .andExpect(status().isOk())
                        .andReturn()
        );
        assertThat(extractLongField(users.path("data"), "id")).contains(createdUserId);

        mockMvc.perform(post("/api/admin/users/{id}/block", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.accountLocked").value(false));

        mockMvc.perform(post("/api/admin/users/{id}/block", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));

        mockMvc.perform(post("/api/admin/users/{id}/unblock", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locked": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountLocked").value(true));

        mockMvc.perform(post("/api/admin/users/{id}/unblock", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locked": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountLocked").value(false));

        mockMvc.perform(post("/api/admin/users/{id}/role", createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "PREMIUM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("PREMIUM"));

        JsonNode metricName = readData(
                mockMvc.perform(post("/api/admin/dict")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "METRIC_NAME",
                                          "label": "Protein"
                                        }
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );
        long metricNameId = metricName.path("id").asLong();
        assertThat(metricName.path("createdAt").asText()).isNotBlank();
        assertThat(metricName.path("updatedAt").asText()).isNotBlank();
        assertThat(metricName.has("chartType")).isFalse();

        JsonNode metricUnit = readData(
                mockMvc.perform(post("/api/admin/dict")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "METRIC_UNIT",
                                          "label": "Gram"
                                        }
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );
        long metricUnitId = metricUnit.path("id").asLong();
        assertThat(metricUnit.has("chartType")).isFalse();

        JsonNode metricNames = readBody(
                mockMvc.perform(get("/api/admin/dict/METRIC_NAME"))
                        .andExpect(status().isOk())
                        .andReturn()
        );
        assertThat(extractLongField(metricNames.path("data"), "id")).contains(metricNameId);

        mockMvc.perform(post("/api/admin/metric-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricNameId": %d,
                                  "metricUnitId": %d
                                }
                                """.formatted(metricNameId, metricUnitId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(metricUnitId))
                .andExpect(jsonPath("$.data.label").value("Gram"));

        mockMvc.perform(get("/api/admin/metric-links/metric-name/{id}/units", metricNameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(metricUnitId));

        mockMvc.perform(post("/api/admin/general-foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dictionaryItemId": %d,
                                  "protein": 10.25,
                                  "fat": 2.50,
                                  "carbs": 5.75,
                                  "callories": 120.00
                                }
                                """.formatted(metricNameId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dictionaryItemId").value(metricNameId));

        mockMvc.perform(get("/api/general-foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].dictionaryItemId").value(metricNameId));

        JsonNode adminTag = readData(
                mockMvc.perform(post("/api/admin/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "#admin-tag"
                                        }
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
        );
        long adminTagId = adminTag.path("id").asLong();

        User otherUser = userRepository.save(User.builder()
                .username("other-user")
                .fullName("Other User")
                .enabled(true)
                .role(Role.USER)
                .build());

        tagRepository.save(Tag.builder()
                .name("#rejected-tag")
                .status(TagStatus.REJECTED)
                .createdBy(otherUser)
                .build());
        tagRepository.save(Tag.builder()
                .name("#deprecated-tag")
                .status(TagStatus.DEPRECATED)
                .createdBy(otherUser)
                .build());
        tagRepository.save(Tag.builder()
                .name("#pending-tag")
                .status(TagStatus.PENDING)
                .createdBy(otherUser)
                .build());

        JsonNode adminTags = readBody(
                mockMvc.perform(get("/api/admin/tags")
                                .param("page", "0")
                                .param("size", "20"))
                        .andExpect(status().isOk())
                        .andReturn()
        );
        assertThat(extractLongField(adminTags.path("data").path("content"), "id")).contains(adminTagId);

        JsonNode sharedTags = readBody(
                mockMvc.perform(get("/api/tags"))
                        .andExpect(status().isOk())
                        .andReturn()
        );
        List<String> sharedTagNames = extractTextField(sharedTags.path("data"), "name");
        assertThat(sharedTagNames)
                .contains("#admin-tag", "#rejected-tag", "#deprecated-tag", "#pending-tag");

        mockMvc.perform(post("/api/admin/tag-chart-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tagId": %d,
                                  "chartType": "PFC_PER_DAY"
                                }
                                """.formatted(adminTagId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagId").value(adminTagId))
                .andExpect(jsonPath("$.data.chartType").value("PFC_PER_DAY"));

        mockMvc.perform(get("/api/admin/tag-chart-types/tag/{tagId}", adminTagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].chartType").value("PFC_PER_DAY"));

        mockMvc.perform(post("/api/admin/database/clear/tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertThat(tagRepository.count()).isZero();
        assertThat(tagChartTypeLinkRepository.count()).isZero();

        mockMvc.perform(post("/api/admin/database/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User desktopUser = userAccountRepository
                .findUserByProviderAndProviderId(ProviderType.LOCAL, "desktop-local-user")
                .orElseThrow();

        assertThat(userRepository.findAll())
                .extracting(User::getUsername)
                .containsExactly(desktopUser.getUsername());
        assertThat(userAccountRepository.findAll()).hasSize(1);
        assertThat(dictionaryRepository.count()).isZero();
        assertThat(generalFoodRepository.count()).isZero();

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(desktopUser.getId()))
                .andExpect(jsonPath("$.data.username").value(desktopUser.getUsername()));

        mockMvc.perform(get("/api/admin/database/table-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    private JsonNode readData(MvcResult result) throws Exception {
        return readBody(result).path("data");
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private List<String> toTextList(JsonNode arrayNode) {
        List<String> values = objectMapper.convertValue(arrayNode, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        values.sort(Comparator.naturalOrder());
        return values;
    }

    private List<String> extractTextField(JsonNode arrayNode, String fieldName) {
        List<String> values = arrayNode.findValues(fieldName).stream()
                .map(JsonNode::asText)
                .sorted()
                .toList();
        return values;
    }

    private List<Long> extractLongField(JsonNode arrayNode, String fieldName) {
        List<Long> values = arrayNode.findValues(fieldName).stream()
                .map(JsonNode::asLong)
                .sorted()
                .toList();
        return values;
    }
}
