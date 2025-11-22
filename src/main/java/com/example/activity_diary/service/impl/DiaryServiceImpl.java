package com.example.activity_diary.service.impl;

import com.example.activity_diary.dto.ActivityItemDto;
import com.example.activity_diary.dto.DiaryEntryCreateDto;
import com.example.activity_diary.dto.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.mappers.DiaryEntryMapper;
import com.example.activity_diary.entity.ActivityItem;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DiaryEntryMapper mapper;

    @Override
    public List<DiaryEntry> getAll() {
        return diaryRepository.findAll();
    }

    @Override
    public Optional<DiaryEntry> getById(Long id) {
        return diaryRepository.findById(id);
    }

    @Override
    public List<DiaryEntry> getByUserId(Long userId) {
        return diaryRepository.findByUserId(userId);
    }

    @Override
    public List<DiaryEntry> getByStatus(String status) {
        try {
            EntryStatus entryStatus = EntryStatus.valueOf(status.toUpperCase());
            return diaryRepository.findByStatus(entryStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + status);
        }
    }

    @Override
    public DiaryEntry create(DiaryEntryCreateDto dto, User user) {

        validateDates(dto.getWhenStarted(), dto.getWhenEnded());

        DiaryEntry entry = mapper.toEntity(dto);

        entry.setWhatHappened(normalize(dto.getWhatHappened()));
        entry.setWhat(normalize(dto.getWhat()));
        entry.setAnyDescription(normalize(dto.getAnyDescription()));

        entry.setUser(user);

        prepareEntry(entry);

        if (dto.getWhatDidYouDo() != null) {
            for (ActivityItemDto itemDto : dto.getWhatDidYouDo()) {
                ActivityItem item = mapper.toActivityItem(itemDto);
                item.setDiaryEntry(entry);
                entry.getWhatDidYouDo().add(item);
            }
        }

        return diaryRepository.save(entry);
    }

    @Override
    public DiaryEntry update(Long id, DiaryEntryUpdateDto dto, UserDetails currentUser) {

        validateDates(dto.getWhenStarted(), dto.getWhenEnded());

        DiaryEntry existing = getByIdForUser(id, currentUser);

        existing.setWhatHappened(normalize(dto.getWhatHappened()));
        existing.setWhat(normalize(dto.getWhat()));
        existing.setWhenStarted(dto.getWhenStarted());
        existing.setWhenEnded(dto.getWhenEnded());
        existing.setHowYouWereFeeling(dto.getHowYouWereFeeling());
        existing.setAnyDescription(normalize(dto.getAnyDescription()));

        if (dto.getStatus() != null) {
            existing.setStatus(EntryStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        computeDuration(existing);

        existing.getWhatDidYouDo().clear();
        if (dto.getWhatDidYouDo() != null) {
            for (ActivityItemDto itemDto : dto.getWhatDidYouDo()) {
                ActivityItem item = mapper.toActivityItem(itemDto);
                item.setDiaryEntry(existing);
                existing.getWhatDidYouDo().add(item);
            }
        }

        // updatedAt будет выставлен через @PreUpdate
        return diaryRepository.save(existing);
    }

    @Override
    public DiaryEntry getByIdForUser(Long id, UserDetails currentUser) {
        DiaryEntry entry = diaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diary entry not found: " + id));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied: entry belongs to another user");
        }

        return entry;
    }

    @Override
    public void delete(Long id, UserDetails currentUser) {
        DiaryEntry entry = diaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        diaryRepository.delete(entry);
    }

    @Override
    public void deleteAll() {
        diaryRepository.deleteAll();
    }

    // ===================== PRIVATE HELPERS =========================

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("End time cannot be earlier than start time");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private void prepareEntry(DiaryEntry entry) {
        if (entry.getStatus() == null) {
            entry.setStatus(EntryStatus.ACTIVE);
        }
        computeDuration(entry);
    }

    private void computeDuration(DiaryEntry entry) {
        if (entry.getWhenStarted() != null && entry.getWhenEnded() != null) {
            long minutes = Duration.between(entry.getWhenStarted(), entry.getWhenEnded()).toMinutes();
            entry.setDuration((int) Math.max(minutes, 0));
        }
    }
}
