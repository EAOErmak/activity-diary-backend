package com.example.activity_diary.security;

import com.example.activity_diary.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, message);
    }

    public void writeForbidden(HttpServletResponse response, String message) throws IOException {
        write(response, HttpStatus.FORBIDDEN, message);
    }

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
