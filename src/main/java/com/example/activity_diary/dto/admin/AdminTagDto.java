package com.example.activity_diary.dto.admin;

import com.example.activity_diary.entity.enums.TagStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTagDto {
    private Long id;
    private String name;
    private TagStatus status;
    private Long createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;
}
