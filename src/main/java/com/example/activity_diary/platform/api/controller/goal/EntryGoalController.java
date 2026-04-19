package com.example.activity_diary.platform.api.controller.goal;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalCreateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalSummaryDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.goal.GoalCalendarService;
import com.example.activity_diary.service.goal.GoalGetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goal/entry")
@RequiredArgsConstructor
public class EntryGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/drop")
    public ApiResponse<DiaryEntryGoalDetailDto> createEntryGoal(@Valid @RequestBody DiaryEntryGoalCreateDto dto) {
        return ApiResponse.ok(
                goalCalendarService.createEntryGoal(
                        currentUserProvider.getCurrentUserId(),
                        dto.getTemplateId(),
                        dto.getTargetDate()
                )
        );
    }

    @PostMapping("/{goalId}/confirm")
    public DiaryEntryGoalDetailDto confirmReal(
            @PathVariable Long goalId,
            @RequestBody DiaryEntryCreateDto dto
    ) {
        return goalCalendarService.confirmEntryGoal(currentUserProvider.getCurrentUserId(), goalId, dto);
    }

    @PostMapping("/{goalId}/confirm-simple")
    public DiaryEntryGoalDetailDto confirmSimple(@PathVariable Long goalId) {
        return goalCalendarService.confirmEntryGoalSimple(currentUserProvider.getCurrentUserId(), goalId);
    }

    @PutMapping("/{goalId}")
    public ApiResponse<DiaryEntryGoalDetailDto> updateConfirmedEntryGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody DiaryEntryUpdateDto dto
    ) {
        return ApiResponse.ok(
                goalCalendarService.updateConfirmedEntryGoal(
                        currentUserProvider.getCurrentUserId(),
                        goalId,
                        dto
                )
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteEntry(@RequestParam Long entryGoalId) {
        goalCalendarService.deleteEntryGoal(currentUserProvider.getCurrentUserId(), entryGoalId);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary/by-date")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listByDate(@RequestParam LocalDate date) {
        return ApiResponse.ok(
                goalGetService.listEntrySummariesByDate(currentUserProvider.getCurrentUserId(), date)
        );
    }

    @GetMapping("/summary/by-day-goal/{dayGoalId}")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listByDayGoal(@PathVariable Long dayGoalId) {
        return ApiResponse.ok(
                goalGetService.listEntrySummariesByDayGoal(currentUserProvider.getCurrentUserId(), dayGoalId)
        );
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<DiaryEntryGoalSummaryDto> getSummary(@PathVariable Long id) {
        return ApiResponse.ok(
                goalGetService.getEntryGoalSummary(currentUserProvider.getCurrentUserId(), id)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DiaryEntryGoalDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(
                goalGetService.getEntryGoalDetail(currentUserProvider.getCurrentUserId(), id)
        );
    }
}
