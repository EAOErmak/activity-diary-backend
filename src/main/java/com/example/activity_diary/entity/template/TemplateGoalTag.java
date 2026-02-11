package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.diary.Tag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "template_goal_tag",
        indexes = {
                @Index(name = "idx_tpl_goal_tag_tpl", columnList = "template_id"),
                @Index(name = "idx_tpl_goal_tag_tag", columnList = "tag_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateGoalTag {

    @EmbeddedId
    private TemplateGoalTagId id;

    @MapsId("templateId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;
}
