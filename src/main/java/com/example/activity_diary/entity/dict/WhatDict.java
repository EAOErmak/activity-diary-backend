package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.base.BaseDictionary;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "what_dict")
@Getter
@Setter
@NoArgsConstructor
public class WhatDict extends BaseDictionary {
    @Column(name = "what_happened_id", nullable = false)
    private Long whatHappenedId;
}
