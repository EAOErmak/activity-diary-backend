package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.goal.GoalCalendarService;
import com.example.activity_diary.service.goal.GoalGetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goal")
@RequiredArgsConstructor
public class GoalCalendarController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;

    @PostMapping("/drop/entry-template")
    public ApiResponse<DiaryEntryGoalDetailDto> createEntryGoal(
            @Valid @RequestBody DiaryEntryGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createEntryGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/drop/day-template")
    public ApiResponse<DayGoalDetailDto> createDayGoal(
            @Valid @RequestBody DayGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createDayGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/drop/week-template")
    public ApiResponse<WeekGoalDetailDto> createWeekGoal(
            @Valid @RequestBody WeekGoalCreateDtp dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createWeekGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/entry/{goalId}/confirm")
    public DiaryEntryGoalDetailDto confirmReal(
            @PathVariable Long goalId,
            @RequestParam Long userId,
            @RequestBody DiaryEntryCreateDto dto
    ) {
        return goalCalendarService.confirmEntryGoal(userId, goalId, dto);
    }

    @PostMapping("/entry/{goalId}/confirm-simple")
    public DiaryEntryGoalDetailDto confirmSimple(
            @PathVariable Long goalId,
            @RequestParam Long userId
    ) {
        return goalCalendarService.confirmEntryGoalSimple(userId, goalId);
    }

    @PutMapping("/entry/{goalId}")
    public ApiResponse<DiaryEntryGoalDetailDto> updateConfirmedEntryGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody DiaryEntryUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.updateConfirmedEntryGoal(user.getId(), goalId, dto));
    }

    @PostMapping("/day/{dayGoalId}/confirm")
    public ApiResponse<DayGoalDetailDto> confirmDay(
            @PathVariable Long dayGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.confirmDayGoal(user.getId(), dayGoalId));
    }

    // Week summary list
    @GetMapping("/week/summary")
    public ApiResponse<List<WeekGoalSummaryDto>> listWeekSummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listWeekSummaries(user.getId(), from, to));
    }

    // Day summary list by range (для календаря)
    @GetMapping("/day/summary")
    public ApiResponse<List<DayGoalSummaryDto>> listDaySummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listDaySummaries(user.getId(), from, to));
    }

    // Entry summaries for конкретной даты (открыли день в календаре)
    @GetMapping("/entry/summary/by-date")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listEntrySummariesByDate(
            @RequestParam LocalDate date,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listEntrySummariesByDate(user.getId(), date));
    }

    // Entry summaries для DayGoal (если на фронте хранишь dayGoalId)
    @GetMapping("/entry/summary/by-day-goal/{dayGoalId}")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listEntrySummariesByDayGoal(
            @PathVariable Long dayGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listEntrySummariesByDayGoal(user.getId(), dayGoalId));
    }

    @GetMapping("/entry/{id}/summary")
    public ApiResponse<DiaryEntryGoalSummaryDto> getEntrySummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getEntryGoalSummary(user.getId(), id));
    }

    @GetMapping("/entry/{id}")
    public ApiResponse<DiaryEntryGoalDetailDto> getEntryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getEntryGoalDetail(user.getId(), id));
    }

    // ===== DayGoal =====
    @GetMapping("/day/{id}/summary")
    public ApiResponse<DayGoalSummaryDto> getDaySummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getDayGoalSummary(user.getId(), id));
    }

    @GetMapping("/day/{id}")
    public ApiResponse<DayGoalDetailDto> getDayDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getDayGoalDetail(user.getId(), id));
    }

    // ===== WeekGoal =====
    @GetMapping("/week/{id}/summary")
    public ApiResponse<WeekGoalSummaryDto> getWeekSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getWeekGoalSummary(user.getId(), id));
    }

    @GetMapping("/week/{id}")
    public ApiResponse<WeekGoalDetailDto> getWeekDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getWeekGoalDetail(user.getId(), id));
    }

    @PostMapping("/replace/week-template")
    public ApiResponse<WeekGoalDetailDto> replaceWeek(
            @RequestBody WeekGoalCreateDtp dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.replaceWeekGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate()));
    }

    @PostMapping("/replace/day-template")
    public ApiResponse<DayGoalDetailDto> replaceDay(
            @RequestBody DayGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.replaceDayGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate()));
    }

    @DeleteMapping("/delete/week")
    public ApiResponse<Void> deleteWeek(
            @RequestParam LocalDate targetDate,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteWeekGoal(user.getId(), targetDate);
        return ApiResponse.ok();
    }

    @DeleteMapping("/delete/day")
    public ApiResponse<Void> deleteDay(
            @RequestParam LocalDate targetDate,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteDayGoal(user.getId(), targetDate);
        return ApiResponse.ok();
    }

    @DeleteMapping("/delete/entry")
    public ApiResponse<Void> deleteEntry(
            @RequestParam Long entryGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteEntryGoal(user.getId(), entryGoalId);
        return ApiResponse.ok();
    }
}
