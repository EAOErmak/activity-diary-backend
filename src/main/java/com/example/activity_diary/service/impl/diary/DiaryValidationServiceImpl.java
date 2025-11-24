package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.diary.DiaryValidationService;
import org.springframework.stereotype.Service;

@Service
public class DiaryValidationServiceImpl implements DiaryValidationService {

    @Override
    public void validateCreate(DiaryEntryCreateDto dto) {

        if (dto.getWhatHappenedId() == null)
            throw new BadRequestException("whatHappenedId is required");

        if (dto.getWhatId() == null)
            throw new BadRequestException("whatId is required");

        if (dto.getWhenStarted() == null)
            throw new BadRequestException("whenStarted is required");
    }
}
