package com.example.activity_diary.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiaryEntryDto {
    private Long id;
    private String whatHappened;
    private String what;
    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;
    private Integer duration;
    private Short howYouWereFeeling;
    private String anyDescription;
    private String status;
    private Long userId;
}
