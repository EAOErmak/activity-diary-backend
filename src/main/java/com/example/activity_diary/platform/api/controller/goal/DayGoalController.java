package com.example.activity_diary.platform.api.controller.goal;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.goal.GoalCalendarService;
import com.example.activity_diary.service.goal.GoalGetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goal/day")
@RequiredArgsConstructor
public class DayGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/drop")
    public ApiResponse<DayGoalDetailDto> createDayGoal(@Valid @RequestBody DayGoalCreateDto dto) {
        return ApiResponse.ok(
                goalCalendarService.createDayGoal(
                        currentUserProvider.getCurrentUserId(),
                        dto.getTemplateId(),
                        dto.getTargetDate()
                )
        );
    }

    @PostMapping("/{dayGoalId}/confirm")
    public ApiResponse<DayGoalDetailDto> confirmDay(@PathVariable Long dayGoalId) {
        return ApiResponse.ok(
                goalCalendarService.confirmDayGoal(currentUserProvider.getCurrentUserId(), dayGoalId)
        );
    }

    @PostMapping("/replace")
    public ApiResponse<DayGoalDetailDto> replaceDay(@RequestBody DayGoalCreateDto dto) {
        return ApiResponse.ok(
                goalCalendarService.replaceDayGoal(
                        currentUserProvider.getCurrentUserId(),
                        dto.getTemplateId(),
                        dto.getTargetDate()
                )
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteDay(@RequestParam LocalDate targetDate) {
        goalCalendarService.deleteDayGoal(currentUserProvider.getCurrentUserId(), targetDate);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary")
    public ApiResponse<List<DayGoalSummaryDto>> listSummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.ok(
                goalGetService.listDaySummaries(currentUserProvider.getCurrentUserId(), from, to)
        );
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<DayGoalSummaryDto> getSummary(@PathVariable Long id) {
        return ApiResponse.ok(goalGetService.getDayGoalSummary(currentUserProvider.getCurrentUserId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<DayGoalDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(goalGetService.getDayGoalDetail(currentUserProvider.getCurrentUserId(), id));
    }
}
