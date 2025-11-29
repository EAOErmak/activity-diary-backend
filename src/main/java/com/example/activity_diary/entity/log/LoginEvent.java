package com.example.activity_diary.entity.log;

import com.example.activity_diary.entity.base.BaseEntity;
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
public class LoginEvent extends BaseEntity {

    private Long userId;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String userAgent;

    @Builder.Default
    @Column(nullable = false)
    private boolean success = false;
}


