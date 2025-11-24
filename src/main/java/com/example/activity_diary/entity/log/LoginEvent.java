package com.example.activity_diary.entity.log;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "login_event")
public class LoginEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String userAgent;

    @Builder.Default
    @Column(nullable = false)
    private boolean success = false;

    @Column(nullable = false)
    private Instant createdAt;
}


