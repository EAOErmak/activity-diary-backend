package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.TagStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tag_name",
                        columnNames = {"name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagStatus status;
}

