package com.example.activity_diary.entity.log;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "registration_event", indexes = {
        @Index(name = "idx_reg_ip", columnList = "ip")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationEvent extends BaseEntity {

    @Column(nullable = false, length = 45) // IPv6-safe
    private String ip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
