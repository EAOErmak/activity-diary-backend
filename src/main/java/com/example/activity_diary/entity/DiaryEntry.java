package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diary_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiaryEntry extends BaseEntity {

    // Category (parent)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "what_happened_id", nullable = false)
    private DictionaryItem whatHappened;

    // Subcategory
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "what_id", nullable = false)
    private DictionaryItem  what;

    @Column(nullable = false)
    private LocalDateTime whenStarted;

    @Column(nullable = false)
    private LocalDateTime whenEnded;

    @Column(nullable = false)
    private Integer duration;

    // Activity items
    @OneToMany(mappedBy = "diaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityItem> whatDidYouDo = new ArrayList<>();

    private Short howYouWereFeeling;

    @Column(length = 1000)
    private String anyDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntryStatus status = EntryStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
