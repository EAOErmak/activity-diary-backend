package com.example.activity_diary;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("desktop")
@AutoConfigureMockMvc
@SpringBootTest(properties = "APP_DB_PATH=./build/desktop-profile-test-${random.uuid}.sqlite")
class DesktopProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void desktopProfileBootstrapsTrustedUserAndAllowsRequestsWithoutAuth() throws Exception {
        assertThat(tableExists("DATABASECHANGELOG")).isTrue();
        assertThat(tableExists("DATABASECHANGELOGLOCK")).isTrue();
        assertThat(appliedBaselineCount()).isGreaterThan(0);

        User desktopUser = userAccountRepository
                .findUserByProviderAndProviderId(ProviderType.LOCAL, "desktop-local-user")
                .orElseThrow();

        assertThat(desktopUser.getUsername()).isEqualTo("desktop");
        assertThat(desktopUser.getRole()).isEqualTo(Role.ADMIN);
        assertThat(desktopUser.isEnabled()).isTrue();
        assertThat(currentUserProvider.getCurrentUserId()).isEqualTo(desktopUser.getId());

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(desktopUser.getId()))
                .andExpect(jsonPath("$.data.username").value(desktopUser.getUsername()));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "desktop@example.local",
                                  "password": "unused"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    private boolean tableExists(String tableName) {
        Integer matches = jdbcTemplate.queryForObject(
                """
                select count(*)
                from sqlite_master
                where type = 'table' and lower(name) = lower(?)
                """,
                Integer.class,
                tableName
        );
        return matches != null && matches > 0;
    }

    private int appliedBaselineCount() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from DATABASECHANGELOG where ID like 'sqlite-baseline-%'",
                Integer.class
        );
        return count == null ? 0 : count;
    }
}
