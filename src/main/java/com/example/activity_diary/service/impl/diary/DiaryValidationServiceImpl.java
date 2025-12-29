package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.diary.DiaryValidationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DiaryValidationServiceImpl implements DiaryValidationService {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final short MIN_MOOD = 0;
    private static final short MAX_MOOD = 10;

    @Override
    public void validateCreate(DiaryEntryCreateDto dto) {

        if (dto.getCategoryId() == null) {
            throw new BadRequestException("whatHappenedId is required");
        }

        if (dto.getSubCategoryId() == null) {
            throw new BadRequestException("whatId is required");
        }

        validateTime(dto.getWhenStarted(), dto.getWhenEnded());

        validateMood(dto.getMood());

        validateDescription(dto.getDescription());
    }

    @Override
    public void validateUpdate(DiaryEntryUpdateDto dto) {

        if (dto.getWhenStarted() != null || dto.getWhenEnded() != null) {
            validateTime(dto.getWhenStarted(), dto.getWhenEnded());
        }

        validateMood(dto.getMood());

        validateDescription(dto.getDescription());
    }

    private void validateTime(LocalDateTime start, LocalDateTime end) {

        if (start == null || end == null) {
            throw new BadRequestException("whenStarted and whenEnded are required");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("whenEnded must be after whenStarted");
        }

        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < 1) {
            throw new BadRequestException("Duration must be at least 1 minute");
        }
    }

    private void validateMood(Short mood) {

        if (mood == null) return;

        if (mood < MIN_MOOD || mood > MAX_MOOD) {
            throw new BadRequestException("Mood must be between 0 and 10");
        }
    }

    private void validateDescription(String desc) {

        if (desc == null) return;

        if (desc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("Description is too long");
        }
    }
}
