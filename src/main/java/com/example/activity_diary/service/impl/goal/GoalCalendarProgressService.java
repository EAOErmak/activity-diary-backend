package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.DiaryEntryCreateMode;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.service.diary.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCalendarProgressService {

    private final DiaryEntryGoalRepository diaryEntryGoalRepository;
    private final WeekGoalRepository weekGoalRepository;
    private final DayGoalRepository dayGoalRepository;
    private final DiaryRepository diaryRepository;

    private final DiaryService diaryService;
    private final GoalMapper goalMapper;
    private final GoalDiaryEntryCommandFactory goalDiaryEntryCommandFactory;

    public DiaryEntryGoalDetailDto confirmEntryGoal(Long userId, Long goalId, DiaryEntryCreateDto dto) {
        DiaryEntryGoal goal = getOwnedEntryGoal(userId, goalId);
        DiaryEntry entry = upsertConfirmedEntry(userId, goal, dto);

        goal.setCurrentEntry(entry);
        recalcUp(goal, entry);

        return goalMapper.toEntryView(goal);
    }

    public DiaryEntryGoalDetailDto confirmEntryGoalSimple(Long userId, Long goalId) {
        DiaryEntryGoal goal = getOwnedEntryGoal(userId, goalId);
        DiaryEntry entry = upsertConfirmedEntry(userId, goal, goalDiaryEntryCommandFactory.toCreateDto(goal));

        goal.setCurrentEntry(entry);
        recalcUp(goal, entry);

        return goalMapper.toEntryView(goal);
    }

    public DiaryEntryGoalDetailDto updateConfirmedEntryGoal(Long userId, Long goalId, DiaryEntryUpdateDto dto) {
        DiaryEntryGoal goal = getOwnedEntryGoal(userId, goalId);

        if (goal.getCurrentEntry() == null) {
            throw new BadRequestException("Goal not confirmed yet");
        }

        DiaryEntryDto updatedDto = diaryService.update(goal.getCurrentEntry().getId(), dto, userId);
        DiaryEntry updatedEntry = getCreatedEntry(userId, updatedDto.getId());

        goal.setCurrentEntry(updatedEntry);
        recalcUp(goal, updatedEntry);

        return goalMapper.toEntryView(goal);
    }

    public DayGoalDetailDto confirmDayGoal(Long userId, Long dayGoalId) {
        DayGoal day = dayGoalRepository.findById(dayGoalId)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));

        if (!day.getWeekGoal().getUser().getId().equals(userId)) {
            throw new NotFoundException("DayGoal not found");
        }

        for (DiaryEntryGoal goal : day.getEntryGoals()) {
            if (goal.getCurrentEntry() != null) {
                continue;
            }

            DiaryEntry createdEntry = createEntry(userId, goalDiaryEntryCommandFactory.toCreateDto(goal));

            goal.setCurrentEntry(createdEntry);
            goal.setCompleteness(100);

            diaryEntryGoalRepository.save(goal);
        }

        day.setCompleteness(100);
        dayGoalRepository.save(day);

        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);
        weekGoalRepository.save(week);

        return goalMapper.toDayView(day);
    }

    private DiaryEntryGoal getOwnedEntryGoal(Long userId, Long goalId) {
        return diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));
    }

    private DiaryEntry upsertConfirmedEntry(Long userId, DiaryEntryGoal goal, DiaryEntryCreateDto dto) {
        DiaryEntry currentEntry = goal.getCurrentEntry();
        if (currentEntry == null || currentEntry.getStatus() == EntryStatus.DELETED) {
            return createEntry(userId, dto);
        }

        DiaryEntryDto updatedDto = diaryService.update(
                currentEntry.getId(),
                goalDiaryEntryCommandFactory.toUpdateDto(dto),
                userId
        );
        return getCreatedEntry(userId, updatedDto.getId());
    }

    private DiaryEntry createEntry(Long userId, DiaryEntryCreateDto dto) {
        DiaryEntryDto createdDto = diaryService.create(dto, userId, DiaryEntryCreateMode.CONFIRM_GOAL);
        return getCreatedEntry(userId, createdDto.getId());
    }

    private DiaryEntry getCreatedEntry(Long userId, Long entryId) {
        return diaryRepository.findGraphByIdAndUser_Id(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
    }

    private void recalcUp(DiaryEntryGoal goal, DiaryEntry entry) {
        GoalCompletenessCalculator.recalcEntryGoal(goal, entry);
        diaryEntryGoalRepository.save(goal);

        DayGoal day = goal.getDayGoal();
        GoalCompletenessCalculator.recalcDayGoal(day);
        dayGoalRepository.save(day);

        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);
        weekGoalRepository.save(week);
    }
}
