package com.example.activity_diary.repository;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByProviderAndProviderId(ProviderType provider, String providerId);

    Optional<UserAccount> findByProviderAndProviderId(ProviderType provider, String providerId);

    @Query("""
            SELECT ua.user
            FROM UserAccount ua
            WHERE ua.provider = :provider
              AND ua.providerId = :providerId
            """)
    Optional<User> findUserByProviderAndProviderId(
            @Param("provider") ProviderType provider,
            @Param("providerId") String providerId
    );
}

