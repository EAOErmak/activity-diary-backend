package com.example.activity_diary.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryUpdateDto {

    @Size(max = 50, message = "Field 'whatHappened' must not exceed 50 characters")
    private String whatHappened;

    @Size(max = 50, message = "Field 'what' must not exceed 50 characters")
    private String what;

    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;

    @Min(value = 1, message = "Field 'howYouWereFeeling' must be between 1 and 5")
    @Max(value = 5, message = "Field 'howYouWereFeeling' must be between 1 and 5")
    private Short howYouWereFeeling;

    @Size(max = 1000, message = "Field 'anyDescription' must not exceed 1000 characters")
    private String anyDescription;

    private String status;

    private List<ActivityItemDto> whatDidYouDo;
}
