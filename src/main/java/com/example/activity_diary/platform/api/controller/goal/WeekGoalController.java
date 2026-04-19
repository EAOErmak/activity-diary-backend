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
@RequestMapping("/api/goal/week")
@RequiredArgsConstructor
public class WeekGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/drop")
    public ApiResponse<WeekGoalDetailDto> createWeekGoal(@Valid @RequestBody WeekGoalCreateDtp dto) {
        return ApiResponse.ok(
                goalCalendarService.createWeekGoal(
                        currentUserProvider.getCurrentUserId(),
                        dto.getTemplateId(),
                        dto.getTargetDate()
                )
        );
    }

    @PostMapping("/replace")
    public ApiResponse<WeekGoalDetailDto> replaceWeek(@RequestBody WeekGoalCreateDtp dto) {
        return ApiResponse.ok(
                goalCalendarService.replaceWeekGoal(
                        currentUserProvider.getCurrentUserId(),
                        dto.getTemplateId(),
                        dto.getTargetDate()
                )
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteWeek(@RequestParam LocalDate targetDate) {
        goalCalendarService.deleteWeekGoal(currentUserProvider.getCurrentUserId(), targetDate);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary")
    public ApiResponse<List<WeekGoalSummaryDto>> listSummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.ok(
                goalGetService.listWeekSummaries(currentUserProvider.getCurrentUserId(), from, to)
        );
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<WeekGoalSummaryDto> getSummary(@PathVariable Long id) {
        return ApiResponse.ok(goalGetService.getWeekGoalSummary(currentUserProvider.getCurrentUserId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<WeekGoalDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(goalGetService.getWeekGoalDetail(currentUserProvider.getCurrentUserId(), id));
    }
}
