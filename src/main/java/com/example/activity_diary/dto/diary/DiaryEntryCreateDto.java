package com.example.activity_diary.dto.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryCreateDto {

    @NotNull(message = "whatHappenedId cannot be null")
    @Min(value = 1, message = "whatHappenedId must be a positive ID")
    private Long whatHappenedId;

    @NotNull(message = "whatId cannot be null")
    @Min(value = 1, message = "whatId must be a positive ID")
    private Long whatId;

    private LocalDateTime whenStarted;

    private LocalDateTime whenEnded;

    @Min(value = 1, message = "howYouWereFeeling must be between 1 and 5")
    @Max(value = 5, message = "howYouWereFeeling must be between 1 and 5")
    private Short howYouWereFeeling;

    @Size(max = 1000, message = "anyDescription must not exceed 1000 characters")
    private String anyDescription;

    // Для статуса лучше использовать whitelist, если у тебя фиксированные значения
    @Pattern(
            regexp = "^(ACTIVE|PLANNED|FINISHED)?$",
            message = "Invalid status value"
    )
    private String status;

    // Вложенные DTO обязательно валидируем через @Valid
    @Valid
    @Size(max = 30, message = "You cannot add more than 30 activity items at once")
    private List<ActivityItemCreateDto> whatDidYouDo;
}
