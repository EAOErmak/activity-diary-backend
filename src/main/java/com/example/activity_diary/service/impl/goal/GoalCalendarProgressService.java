package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryDto;
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
import com.example.activity_diary.service.impl.diary.EntryMetricDetailsLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final EntryMetricDetailsLoader entryMetricDetailsLoader;

    public DiaryEntryGoalDetailDto confirmEntryGoal(Long userId, Long goalId, GoalDiaryEntryCommand command) {
        DiaryEntryGoal goal = getOwnedEntryGoalWithDetails(userId, goalId);
        DiaryEntry entry = upsertConfirmedEntry(userId, goal, command);

        goal.setCurrentEntry(entry);
        recalcUp(goal, entry);

        return goalMapper.toEntryView(goal);
    }

    public DiaryEntryGoalDetailDto confirmEntryGoalSimple(Long userId, Long goalId) {
        DiaryEntryGoal goal = getOwnedEntryGoalWithDetails(userId, goalId);
        DiaryEntry entry = upsertConfirmedEntry(userId, goal, goalDiaryEntryCommandFactory.fromGoal(goal));

        goal.setCurrentEntry(entry);
        recalcUp(goal, entry);

        return goalMapper.toEntryView(goal);
    }

    public DiaryEntryGoalDetailDto updateConfirmedEntryGoal(Long userId, Long goalId, GoalDiaryEntryCommand command) {
        DiaryEntryGoal goal = getOwnedEntryGoalWithDetails(userId, goalId);

        if (goal.getCurrentEntry() == null) {
            throw new BadRequestException("Goal not confirmed yet");
        }

        DiaryEntryDto updatedDto = diaryService.update(
                goal.getCurrentEntry().getId(),
                goalDiaryEntryCommandFactory.toUpdateDto(command),
                userId
        );
        DiaryEntry updatedEntry = getCreatedEntry(userId, updatedDto.getId());

        goal.setCurrentEntry(updatedEntry);
        recalcUp(goal, updatedEntry);

        return goalMapper.toEntryView(goal);
    }

    public DayGoalDetailDto confirmDayGoal(Long userId, Long dayGoalId) {
        DayGoal day = dayGoalRepository.findDetailByIdAndWeekGoal_User_Id(dayGoalId, userId)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));
        initializeGoalDetails(day.getEntryGoals());

        List<DiaryEntryGoal> goalsToCreate = day.getEntryGoals().stream()
                .filter(goal -> goal.getCurrentEntry() == null)
                .toList();

        Map<Long, DiaryEntry> createdEntriesByGoalId = createEntriesForGoals(userId, goalsToCreate);
        for (DiaryEntryGoal goal : day.getEntryGoals()) {
            DiaryEntry createdEntry = createdEntriesByGoalId.get(goal.getId());
            if (createdEntry != null) {
                goal.setCurrentEntry(createdEntry);
                goal.setCompleteness(100);
            }
        }

        day.setCompleteness(100);

        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);

        return goalMapper.toDayView(day);
    }

    private DiaryEntryGoal getOwnedEntryGoal(Long userId, Long goalId) {
        return diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));
    }

    private DiaryEntryGoal getOwnedEntryGoalWithDetails(Long userId, Long goalId) {
        DiaryEntryGoal goal = getOwnedEntryGoal(userId, goalId);
        initializeGoalDetails(List.of(goal));
        return goal;
    }

    private DiaryEntry upsertConfirmedEntry(Long userId, DiaryEntryGoal goal, GoalDiaryEntryCommand command) {
        DiaryEntry currentEntry = goal.getCurrentEntry();
        if (currentEntry == null || currentEntry.getStatus() == EntryStatus.DELETED) {
            return createEntry(userId, command);
        }

        DiaryEntryDto updatedDto = diaryService.update(
                currentEntry.getId(),
                goalDiaryEntryCommandFactory.toFinishedUpdateDto(command),
                userId
        );
        return getCreatedEntry(userId, updatedDto.getId());
    }

    private DiaryEntry createEntry(Long userId, GoalDiaryEntryCommand command) {
        DiaryEntryDto createdDto = diaryService.create(
                goalDiaryEntryCommandFactory.toCreateDto(command),
                userId,
                DiaryEntryCreateMode.CONFIRM_GOAL
        );
        return getCreatedEntry(userId, createdDto.getId());
    }

    private Map<Long, DiaryEntry> createEntriesForGoals(Long userId, List<DiaryEntryGoal> goals) {
        if (goals == null || goals.isEmpty()) {
            return Map.of();
        }

        List<DiaryEntryDto> createdEntries = diaryService.createAll(
                goals.stream()
                        .map(goalDiaryEntryCommandFactory::fromGoal)
                        .map(goalDiaryEntryCommandFactory::toCreateDto)
                        .toList(),
                userId,
                DiaryEntryCreateMode.CONFIRM_GOAL
        );
        Map<Long, DiaryEntry> entriesById = loadEntriesById(
                userId,
                createdEntries.stream()
                        .map(DiaryEntryDto::getId)
                        .toList()
        );

        Map<Long, DiaryEntry> entriesByGoalId = new LinkedHashMap<>();
        for (int index = 0; index < goals.size(); index++) {
            Long entryId = createdEntries.get(index).getId();
            DiaryEntry entry = entriesById.get(entryId);
            if (entry == null) {
                throw new NotFoundException("Entry not found");
            }
            entriesByGoalId.put(goals.get(index).getId(), entry);
        }

        return entriesByGoalId;
    }

    private DiaryEntry getCreatedEntry(Long userId, Long entryId) {
        DiaryEntry entry = diaryRepository.findGraphByIdAndUser_Id(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        entryMetricDetailsLoader.loadForEntry(entryId);
        return entry;
    }

    private Map<Long, DiaryEntry> loadEntriesById(Long userId, List<Long> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }

        List<DiaryEntry> entries = diaryRepository.findAllGraphByIdInAndUser_Id(entryIds, userId);
        entryMetricDetailsLoader.loadForEntries(entryIds);
        return entries.stream()
                .collect(java.util.stream.Collectors.toMap(DiaryEntry::getId, entry -> entry));
    }

    private void recalcUp(DiaryEntryGoal goal, DiaryEntry entry) {
        GoalCompletenessCalculator.recalcEntryGoal(goal, entry);

        DayGoal day = goal.getDayGoal();
        GoalCompletenessCalculator.recalcDayGoal(day);

        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);
    }

    private void initializeGoalDetails(Collection<DiaryEntryGoal> goals) {
        List<Long> goalIds = goals == null
                ? List.of()
                : goals.stream()
                        .map(DiaryEntryGoal::getId)
                        .filter(java.util.Objects::nonNull)
                        .toList();

        if (!goalIds.isEmpty()) {
            diaryEntryGoalRepository.findAllMetricDetailsByIdIn(goalIds);
        }
    }
}
