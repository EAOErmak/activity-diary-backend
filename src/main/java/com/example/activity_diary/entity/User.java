package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_chat_id", columnList = "chat_id"),
                @Index(name = "idx_user_locked", columnList = "account_locked"),
                @Index(name = "idx_user_created", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 64)
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 128)
    private String fullName;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "chat_id", unique = true)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    @Builder.Default
    @Column(name = "failed_2fa_attempts", nullable = false)
    private int failed2faAttempts = 0;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DiaryEntry> diaryEntries = new ArrayList<>();

    /* ===== Бизнес-методы ===== */

    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = newRole;
    }

    public void bindChatId(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("ChatId cannot be null");
        }
        this.chatId = chatId;
    }

    public void enable() {
        this.enabled = true;
    }

    public void lockUntil(LocalDateTime time) {
        this.accountLocked = true;
        this.lockUntil = time;
    }

    public void unlock() {
        this.accountLocked = false;
        this.lockUntil = null;
        this.failed2faAttempts = 0;
    }

    public boolean isCurrentlyLocked() {
        return accountLocked && lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    public void increaseFailed2faAttempts() {
        this.failed2faAttempts++;
    }

    public void resetFailed2faAttempts() {
        this.failed2faAttempts = 0;
    }
}
