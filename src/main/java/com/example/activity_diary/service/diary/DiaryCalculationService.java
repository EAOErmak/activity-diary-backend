package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DiaryCalculationService {

    public void normalizeEntry(DiaryEntryCreateDto dto, DiaryEntry entry) {
        entry.setAnyDescription(normalize(dto.getAnyDescription()));
        entry.setHowYouWereFeeling(dto.getHowYouWereFeeling());
        entry.setWhenStarted(dto.getWhenStarted());
        entry.setWhenEnded(dto.getWhenEnded());
        // статус обрабатывается в DiaryServiceImpl
    }

    public void normalizeEntry(DiaryEntryUpdateDto dto, DiaryEntry entry) {
        if (dto.getAnyDescription() != null)
            entry.setAnyDescription(normalize(dto.getAnyDescription()));

        entry.setHowYouWereFeeling(dto.getHowYouWereFeeling());
        entry.setWhenStarted(dto.getWhenStarted());
        entry.setWhenEnded(dto.getWhenEnded());

        // статус обрабатывается в DiaryServiceImpl
    }

    public void computeDuration(DiaryEntry entry) {
        if (entry.getWhenStarted() != null && entry.getWhenEnded() != null) {
            long minutes = Duration.between(entry.getWhenStarted(), entry.getWhenEnded()).toMinutes();
            entry.setDuration((int) Math.max(0, minutes));
        } else {
            entry.setDuration(null);
        }
    }

    private String normalize(String v) {
        return v == null ? null : v.trim();
    }
}
