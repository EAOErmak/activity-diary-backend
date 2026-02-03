package com.example.activity_diary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTagId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
