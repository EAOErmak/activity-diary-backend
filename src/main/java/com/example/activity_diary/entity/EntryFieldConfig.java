package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "entry_field_config")
@Getter
@Setter
public class EntryFieldConfig extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name; // ✅ имя шаблона конфига

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "what_happened_id")
    private DictionaryItem whatHappened;

    private Boolean showWhat;
    private Boolean showActivities;
    private Boolean showFeeling;
    private Boolean showDescription;

    private Boolean requiredWhat;
    private Boolean requiredActivities;
}
