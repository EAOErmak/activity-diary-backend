package com.example.activity_diary.dto.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryUpdateDto {

    @Min(value = 1, message = "whatHappenedId must be a positive ID")
    private Long whatHappenedId;

    @Min(value = 1, message = "whatId must be a positive ID")
    private Long whatId;

    @PastOrPresent(message = "whenStarted cannot be in the future")
    private LocalDateTime whenStarted;

    @PastOrPresent(message = "whenEnded cannot be in the future")
    private LocalDateTime whenEnded;

    @Min(value = 1, message = "howYouWereFeeling must be between 1 and 5")
    @Max(value = 5, message = "howYouWereFeeling must be between 1 and 5")
    private Short howYouWereFeeling;

    @Size(max = 1000, message = "anyDescription must not exceed 1000 characters")
    private String anyDescription;

    @Pattern(
            regexp = "^(ACTIVE|PLANNED|FINISHED)?$",
            message = "Invalid status value"
    )
    private String status;

    @Valid
    @Size(max = 30, message = "You cannot add more than 30 activity items at once")
    private List<ActivityItemUpdateDto> whatDidYouDo;
}
