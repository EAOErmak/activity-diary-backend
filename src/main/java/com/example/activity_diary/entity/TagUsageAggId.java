package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.TagUsageBucket;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class TagUsageAggId implements Serializable {
    private Long userId;
    private Long tagId;

    @Enumerated(EnumType.STRING)
    private TagUsageBucket bucket;

    private LocalDate bucketStart;
}
