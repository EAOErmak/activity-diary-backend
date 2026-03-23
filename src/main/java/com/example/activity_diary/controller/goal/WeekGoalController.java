package com.example.activity_diary.controller.goal;

import com.example.activity_diary.dto.ApiResponse;
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
@RequestMapping("/api/goal/week")
@RequiredArgsConstructor
public class WeekGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;

    @PostMapping("/drop")
    public ApiResponse<WeekGoalDetailDto> createWeekGoal(
            @Valid @RequestBody WeekGoalCreateDtp dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createWeekGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/replace")
    public ApiResponse<WeekGoalDetailDto> replaceWeek(
            @RequestBody WeekGoalCreateDtp dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.replaceWeekGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteWeek(
            @RequestParam LocalDate targetDate,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteWeekGoal(user.getId(), targetDate);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary")
    public ApiResponse<List<WeekGoalSummaryDto>> listSummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listWeekSummaries(user.getId(), from, to));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<WeekGoalSummaryDto> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getWeekGoalSummary(user.getId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<WeekGoalDetailDto> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getWeekGoalDetail(user.getId(), id));
    }
}