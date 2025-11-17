package com.example.activity_diary.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_entry_id", nullable = false)
    @JsonIgnore
    private DiaryEntry diaryEntry;
}

