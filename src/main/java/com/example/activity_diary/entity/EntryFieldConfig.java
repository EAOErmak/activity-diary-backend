package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "entry_field_config")
@Getter
@Setter
public class EntryFieldConfig extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name; // имя шаблона конфига

    // ✅ ЛОГИКА ОТОБРАЖЕНИЯ
    private Boolean showSubCategory;
    private Boolean showMetrics;
    private Boolean showMood;
    private Boolean showDescription;

    // ✅ ЛОГИКА ОБЯЗАТЕЛЬНОСТИ
    private Boolean requiredSubCategory;
    private Boolean requiredMetrics;
}
