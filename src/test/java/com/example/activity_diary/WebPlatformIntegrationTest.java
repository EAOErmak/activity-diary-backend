package com.example.activity_diary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("web")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:./build/web-profile-test.sqlite",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect",
        "spring.liquibase.enabled=false",
        "jwt.secret=12345678901234567890123456789012",
        "jwt.access-expiration-ms=60000",
        "jwt.refresh-expiration-ms=120000"
})
class WebPlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void webProfileExposesAuthEndpointsAndProtectsSharedApi() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@example.local",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }
}
