package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.SessionState;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramSession {

    @Id
    @Column(name = "chat_id")
    private Long chatId;          // chatId пользователя в Telegram

    @Column(nullable = false)
    private String email;         // email, который он указал в /verify

    @Column(name = "code", nullable = false)
    private String code;          // одноразовый 6-значный код

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // срок жизни кода

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private SessionState state;
}
