package com.example.activity_diary.rate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Global-limit
        if (!rateLimitService.tryGlobalConsume(request)) {
            write429(response, "Too many requests (global limit)");
            return;
        }

        // Method-level limit using @RateLimit
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (handler instanceof HandlerMethod method) {

            RateLimit annotation = method.getMethodAnnotation(RateLimit.class);

            if (annotation != null) {
                if (!rateLimitService.tryConsumeForAnnotation(request, annotation)) {
                    write429(response, "Too many requests");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void write429(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(429);
        resp.setContentType("application/json");
        resp.getWriter().write(
                "{\"success\":false,\"message\":\"" + msg.replace("\"", "\\\"") + "\"}"
        );
    }
}
