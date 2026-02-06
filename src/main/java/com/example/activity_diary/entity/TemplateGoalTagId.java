package com.example.activity_diary.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TemplateGoalTagId implements java.io.Serializable {
    private Long templateId;
    private Long tagId;
}

