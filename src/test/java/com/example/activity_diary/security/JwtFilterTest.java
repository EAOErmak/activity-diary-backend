package com.example.activity_diary.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void doFilterInternal_invalidToken_returnsApiResponseError() throws ServletException, IOException {
        JwtFilter filter = new JwtFilter(jwtUtils, objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtUtils.isAccessTokenValid("invalid-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertFalse(body.get("success").asBoolean());
        assertEquals("Invalid or expired token", body.get("message").asText());
        assertEquals(true, body.get("data").isNull());
    }

    @Test
    void doFilterInternal_missingAuthorizationHeader_passesThrough() throws ServletException, IOException {
        JwtFilter filter = new JwtFilter(jwtUtils, objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verifyNoInteractions(jwtUtils);
    }
}
