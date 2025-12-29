package com.example.activity_diary.repository;

import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByProviderAndProviderId(ProviderType provider, String providerId);
    Optional<UserAccount> findByProviderAndProviderId(ProviderType provider, String providerId);
}

