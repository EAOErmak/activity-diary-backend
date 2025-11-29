package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.SuggestionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dictionary_suggestion")
public class DictionarySuggestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "dict_type", nullable = false)
    private DictionaryType dictType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    private Long moderatorId;
    private String moderatorComment;
    private LocalDateTime reviewedAt;
}
