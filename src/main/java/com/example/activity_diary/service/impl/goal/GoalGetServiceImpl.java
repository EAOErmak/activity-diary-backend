package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.dto.mapper.GoalGetMapper;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.service.goal.GoalGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalGetServiceImpl implements GoalGetService {

    private final DiaryEntryGoalRepository entryGoalRepo;
    private final DayGoalRepository dayGoalRepo;
    private final WeekGoalRepository weekGoalRepo;
    private final GoalGetMapper goalGetMapper;

    @Override
    @Transactional(readOnly = true)
    public DiaryEntryGoalSummaryDto getEntryGoalSummary(Long userId, Long goalId) {
        var g = entryGoalRepo.findSummaryByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));
        return goalGetMapper.toEntrySummary(g);
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryEntryGoalDetailDto getEntryGoalDetail(Long userId, Long goalId) {
        var g = entryGoalRepo.findDetailByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));
        return goalGetMapper.toEntryDetail(g);
    }

    @Override
    @Transactional(readOnly = true)
    public DayGoalSummaryDto getDayGoalSummary(Long userId, Long dayGoalId) {
        var d = dayGoalRepo.findSummaryByIdAndWeekGoal_User_Id(dayGoalId, userId)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));
        return goalGetMapper.toDaySummary(d);
    }

    @Override
    @Transactional(readOnly = true)
    public DayGoalDetailDto getDayGoalDetail(Long userId, Long dayGoalId) {
        var d = dayGoalRepo.findDetailByIdAndWeekGoal_User_Id(dayGoalId, userId)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));
        return goalGetMapper.toDayDetail(d);
    }

    @Override
    @Transactional(readOnly = true)
    public WeekGoalSummaryDto getWeekGoalSummary(Long userId, Long weekGoalId) {
        var w = weekGoalRepo.findSummaryByIdAndUser_Id(weekGoalId, userId)
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));
        return goalGetMapper.toWeekSummary(w);
    }

    @Override
    @Transactional(readOnly = true)
    public WeekGoalDetailDto getWeekGoalDetail(Long userId, Long weekGoalId) {
        var w = weekGoalRepo.findDetailByIdAndUser_Id(weekGoalId, userId)
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));
        return goalGetMapper.toWeekDetail(w);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeekGoalSummaryDto> listWeekSummaries(Long userId, LocalDate from, LocalDate to) {

        if (from == null || to == null) {
            throw new BadRequestException("from/to are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to must be >= from");
        }

        // РЅРѕСЂРјР°Р»РёР·СѓРµРј Рє РіСЂР°РЅРёС†Р°Рј РЅРµРґРµР»СЊ (РџРЅ..Р’СЃ) С‡С‚РѕР±С‹ С„СЂРѕРЅС‚Сѓ Р±С‹Р»Рѕ СѓРґРѕР±РЅРµРµ
        LocalDate fromMonday = from.with(DayOfWeek.MONDAY);
        LocalDate toSunday = to.with(DayOfWeek.SUNDAY);

        ZoneId zone = ZoneId.systemDefault();

        Instant fromInstant = fromMonday.atStartOfDay(zone).toInstant();
        Instant toInstant = toSunday.atTime(23, 59, 59).atZone(zone).toInstant();

        List<WeekGoal> weeks = weekGoalRepo.findByUserIdAndRange(userId, fromInstant, toInstant);

        weeks.sort(Comparator.comparing(WeekGoal::getWhenStarted));

        return goalGetMapper.toWeekSummaryList(weeks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DayGoalSummaryDto> listDaySummaries(Long userId, LocalDate from, LocalDate to) {
        return goalGetMapper.toDaySummaryList(dayGoalRepo.findAllByUserAndDateRange(userId, from, to));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryGoalSummaryDto> listEntrySummariesByDate(Long userId, LocalDate date) {
        return goalGetMapper.toEntrySummaryList(entryGoalRepo.findAllByUserAndTargetDate(userId, date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryGoalSummaryDto> listEntrySummariesByDayGoal(Long userId, Long dayGoalId) {
        return goalGetMapper.toEntrySummaryList(entryGoalRepo.findAllByUserAndDayGoal(userId, dayGoalId));
    }
}
