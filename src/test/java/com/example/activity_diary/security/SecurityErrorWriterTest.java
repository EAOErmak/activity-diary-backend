package com.example.activity_diary.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityErrorWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityErrorWriter securityErrorWriter = new SecurityErrorWriter(objectMapper);

    @Test
    void writeUnauthorized_writesUnifiedApiResponse() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityErrorWriter.writeUnauthorized(response, "Authentication required");

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertFalse(body.get("success").asBoolean());
        assertEquals("Authentication required", body.get("message").asText());
        assertTrue(body.get("data").isNull());
    }
}
