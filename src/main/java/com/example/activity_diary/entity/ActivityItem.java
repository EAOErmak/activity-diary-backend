package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "activity_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ActivityItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dict_name_id", nullable = false)
    private ActivityItemNameDict name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dict_unit_id", nullable = false)
    private ActivityItemUnitDict unit;

    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_entry_id", nullable = false)
    @JsonIgnore
    private DiaryEntry diaryEntry;
}
