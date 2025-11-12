package com.example.activity_diary.service.impl;

import com.example.activity_diary.entity.ActivityItem;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.EntryStatus;
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
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    @Override
    public DiaryEntry create(DiaryEntry entry) {
        prepareEntry(entry);
        return diaryRepository.save(entry);
    }

    @Override
    public DiaryEntry update(Long id, DiaryEntry updated) {
        DiaryEntry existing = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary entry not found with id " + id));

        // Обновляем поля
        existing.setWhatHappened(updated.getWhatHappened());
        existing.setWhat(updated.getWhat());
        existing.setWhenStarted(updated.getWhenStarted());
        existing.setWhenEnded(updated.getWhenEnded());
        existing.setHowYouWereFeeling(updated.getHowYouWereFeeling());
        existing.setAnyDescription(updated.getAnyDescription());
        existing.setStatus(updated.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        // Считаем длительность, если есть даты
        computeDuration(existing);

        // Обновляем активити
        existing.getWhatDidYouDo().clear();
        if (updated.getWhatDidYouDo() != null) {
            for (ActivityItem item : updated.getWhatDidYouDo()) {
                item.setDiaryEntry(existing);
                existing.getWhatDidYouDo().add(item);
            }
        }

        return diaryRepository.save(existing);
    }

    @Override
    public DiaryEntry getByIdForUser(Long id, UserDetails currentUser) {
        DiaryEntry entry = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary entry not found with id " + id));

        // Загружаем реального пользователя из базы по username (email)
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем владельца
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: this entry does not belong to the current user");
        }

        return entry;
    }

    @Override
    public void delete(Long id) {
        diaryRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        diaryRepository.deleteAll();
    }

    // =============================================================
    // 🧠 Дополнительные приватные методы
    // =============================================================

    private void prepareEntry(DiaryEntry entry) {
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        if (entry.getStatus() == null) {
            entry.setStatus(EntryStatus.ACTIVE);
        }

        computeDuration(entry);

        if (entry.getWhatDidYouDo() != null) {
            for (ActivityItem item : entry.getWhatDidYouDo()) {
                item.setDiaryEntry(entry);
            }
        }
    }

    private void computeDuration(DiaryEntry entry) {
        if (entry.getWhenStarted() != null && entry.getWhenEnded() != null) {
            long minutes = Duration.between(entry.getWhenStarted(), entry.getWhenEnded()).toMinutes();
            entry.setDuration((int) Math.max(minutes, 0));
        }
    }
}
