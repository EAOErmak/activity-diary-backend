package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.DictionaryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "dictionary_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "label"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItem extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictionaryType type;   // WHAT, WHAT_HAPPENED, UNIT, ITEM_NAME

    @Column(nullable = false)
    private String label;          // текст, который видит пользователь

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "allowed_role", length = 50)
    private String allowedRole;    // USER / ADMIN / PREMIUM и т.п.
}
