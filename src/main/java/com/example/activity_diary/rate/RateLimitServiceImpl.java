package com.example.activity_diary.rate;

import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> globalBucketCache = new ConcurrentHashMap<>();

    // Глобальный лимит на IP (fail-safe)
    private static final Bandwidth GLOBAL_LIMIT = Bandwidth.classic(
            100,
            Refill.greedy(100, Duration.ofMinutes(1))
    );

    @Override
    public boolean tryGlobalConsume(HttpServletRequest request) {

        String ip = extractIp(request);
        String key = "global:" + ip;

        Bucket bucket = globalBucketCache.computeIfAbsent(
                key,
                k -> Bucket4j.builder().addLimit(GLOBAL_LIMIT).build()
        );

        boolean ok = bucket.tryConsume(1);

        if (!ok) {
            log.warn("GLOBAL RATE LIMIT exceeded → IP={}", ip);
        }

        return ok;
    }

    @Override
    public boolean tryConsumeForAnnotation(HttpServletRequest request, RateLimit annotation) {

        if (annotation == null) return true;

        String ip = extractIp(request);
        String key;

        if (annotation.usernameBased()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated())
                    ? auth.getName()
                    : "anonymous";

            key = "usr:" + username + ":" + ip + ":" + request.getRequestURI();
        } else {
            key = "ip:" + ip + ":" + request.getRequestURI();
        }

        Bucket bucket = bucketCache.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(
                    annotation.capacity(),
                    Refill.greedy(
                            annotation.refillTokens(),
                            Duration.ofSeconds(annotation.refillPeriodSeconds())
                    )
            );
            return Bucket4j.builder().addLimit(limit).build();
        });

        boolean ok = bucket.tryConsume(1);

        if (!ok) {
            log.warn(
                    "RATE LIMIT exceeded → key={}, path={}, ip={}",
                    key,
                    request.getRequestURI(),
                    ip
            );
        }

        return ok;
    }

    private String extractIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0];
        }
        return req.getRemoteAddr();
    }
}
