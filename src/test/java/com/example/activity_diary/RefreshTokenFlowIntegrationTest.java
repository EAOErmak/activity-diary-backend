package com.example.activity_diary;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.platform.web.auth.service.AuthService;
import com.example.activity_diary.platform.web.auth.service.RefreshTokenService;
import com.example.activity_diary.repository.RefreshTokenRepository;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("web")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:./build/refresh-flow-test.sqlite",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect",
        "spring.liquibase.enabled=false",
        "jwt.secret=12345678901234567890123456789012",
        "jwt.access-expiration-ms=60000",
        "jwt.refresh-expiration-ms=120000"
})
class RefreshTokenFlowIntegrationTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void refresh_rotatesTokenAndRejectsOldTokenReuse() {
        TestUser testUser = createUser("rotate");
        AuthResponseDto login = login(testUser);

        AuthResponseDto refreshed = authService.refresh(login.getRefreshToken());

        assertNotEquals(login.getRefreshToken(), refreshed.getRefreshToken());
        assertNotNull(refreshed.getAccessToken());

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(testUser.user());
        assertEquals(2, tokens.size());

        RefreshToken original = tokens.stream()
                .filter(token -> digestToBase64(login.getRefreshToken()).equals(token.getTokenHash()))
                .findFirst()
                .orElseThrow();

        assertTrue(original.isRevoked());
        assertThrows(
                ForbiddenException.class,
                () -> authService.refresh(login.getRefreshToken())
        );
    }

    @Test
    void refresh_rejectsExpiredToken() {
        TestUser testUser = createUser("expired");
        String expiredToken = createExpiredRefreshToken(testUser.user());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(testUser.user())
                        .tokenHash(digestToBase64(expiredToken))
                        .expiresAt(Instant.now().minusSeconds(30))
                        .build()
        );

        assertThrows(ForbiddenException.class, () -> authService.refresh(expiredToken));
    }

    @Test
    void refresh_rejectsRevokedToken() {
        TestUser testUser = createUser("revoked");
        AuthResponseDto login = login(testUser);

        refreshTokenService.revokeByToken(login.getRefreshToken());

        assertThrows(
                ForbiddenException.class,
                () -> authService.refresh(login.getRefreshToken())
        );
    }

    @Test
    void parallelRefresh_sameToken_onlyOneRequestSucceeds() throws Exception {
        TestUser testUser = createUser("parallel");
        AuthResponseDto login = login(testUser);
        String rawRefreshToken = login.getRefreshToken();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        try {
            Callable<RefreshAttempt> task = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    return new RefreshAttempt(authService.refresh(rawRefreshToken), null);
                } catch (Exception e) {
                    return new RefreshAttempt(null, e);
                }
            };

            Future<RefreshAttempt> first = executor.submit(task);
            Future<RefreshAttempt> second = executor.submit(task);

            RefreshAttempt attemptOne = first.get(30, TimeUnit.SECONDS);
            RefreshAttempt attemptTwo = second.get(30, TimeUnit.SECONDS);

            long successCount = List.of(attemptOne, attemptTwo).stream()
                    .filter(attempt -> attempt.response() != null)
                    .count();
            long failureCount = List.of(attemptOne, attemptTwo).stream()
                    .filter(attempt -> attempt.error() != null)
                    .count();

            assertEquals(1, successCount);
            assertEquals(1, failureCount);

            Throwable error = List.of(attemptOne, attemptTwo).stream()
                    .map(RefreshAttempt::error)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElseThrow();
            assertInstanceOf(ForbiddenException.class, error);

            List<RefreshToken> allTokens = refreshTokenRepository.findAllByUser(testUser.user());
            List<RefreshToken> activeTokens = refreshTokenRepository.findActiveByUser(
                    testUser.user(),
                    Instant.now()
            );

            assertEquals(2, allTokens.size());
            assertEquals(1, activeTokens.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void logout_revokesCurrentRefreshToken() {
        TestUser testUser = createUser("logout");
        AuthResponseDto login = login(testUser);

        authService.logout(login.getRefreshToken());

        assertEquals(
                0,
                refreshTokenRepository.findActiveByUser(testUser.user(), Instant.now()).size()
        );
        assertThrows(
                ForbiddenException.class,
                () -> authService.refresh(login.getRefreshToken())
        );
    }

    private TestUser createUser(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .username(prefix + "_" + suffix)
                .fullName("Test User")
                .enabled(true)
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String email = prefix + "_" + suffix + "@example.com";
        UserAccount account = UserAccount.builder()
                .user(user)
                .provider(ProviderType.LOCAL)
                .providerId(email)
                .passwordHash(passwordEncoder.encode("secret123"))
                .build();
        userAccountRepository.save(account);

        return new TestUser(user, email);
    }

    private AuthResponseDto login(TestUser testUser) {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail(testUser.email());
        request.setPassword("secret123");

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("127.0.0.1");
        httpRequest.addHeader("User-Agent", "JUnit");

        return authService.login(request, httpRequest);
    }

    private String createExpiredRefreshToken(User user) {
        Key signingKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setId(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now.minusSeconds(120)))
                .setExpiration(Date.from(now.minusSeconds(60)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static String digestToBase64(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private record TestUser(User user, String email) {
    }

    private record RefreshAttempt(AuthResponseDto response, Throwable error) {
    }
}
