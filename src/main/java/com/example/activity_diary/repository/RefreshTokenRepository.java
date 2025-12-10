package com.example.activity_diary.repository;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // ✅ Только активный токен (не revoked и не просрочен)
    @Query("""
        SELECT t FROM RefreshToken t
        WHERE t.tokenHash = :tokenHash
          AND t.revoked = false
          AND t.expiresAt > :now
    """)
    Optional<RefreshToken> findActiveByTokenHash(String tokenHash, Instant now);

    // ✅ Все токены пользователя (для logout-all или аудита)
    List<RefreshToken> findAllByUser(User user);

    // ✅ Массовый logout (все токены пользователя)
    @Modifying
    @Query("""
        UPDATE RefreshToken t
        SET t.revoked = true
        WHERE t.user = :user
    """)
    void revokeAllByUser(User user);

    // ✅ Очистка мусора (expired)
    @Modifying
    @Query("""
        DELETE FROM RefreshToken t
        WHERE t.expiresAt < :now
    """)
    void deleteAllExpired(Instant now);
}
