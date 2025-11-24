package com.example.activity_diary.dto.diary;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryDto {

    private Long id;

    // ID + Name структуры словаря
    private Long whatHappenedId;
    private String whatHappenedName;

    private Long whatId;
    private String whatName;

    // Сами поля дневника
    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;
    private Integer duration;

    private Short howYouWereFeeling;
    private String anyDescription;
    private String status;

    // Пользователь
    private Long userId;

    // Вложенные items
    private List<ActivityItemResponseDto> whatDidYouDo;

    // Auditing (BaseEntity)
    private Instant createdAt;
    private Instant updatedAt;
}
