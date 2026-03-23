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
@RequestMapping("/api/goal/day")
@RequiredArgsConstructor
public class DayGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;

    @PostMapping("/drop")
    public ApiResponse<DayGoalDetailDto> createDayGoal(
            @Valid @RequestBody DayGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createDayGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/{dayGoalId}/confirm")
    public ApiResponse<DayGoalDetailDto> confirmDay(
            @PathVariable Long dayGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.confirmDayGoal(user.getId(), dayGoalId)
        );
    }

    @PostMapping("/replace")
    public ApiResponse<DayGoalDetailDto> replaceDay(
            @RequestBody DayGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.replaceDayGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteDay(
            @RequestParam LocalDate targetDate,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteDayGoal(user.getId(), targetDate);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary")
    public ApiResponse<List<DayGoalSummaryDto>> listSummaries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listDaySummaries(user.getId(), from, to));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<DayGoalSummaryDto> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getDayGoalSummary(user.getId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<DayGoalDetailDto> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getDayGoalDetail(user.getId(), id));
    }
}
