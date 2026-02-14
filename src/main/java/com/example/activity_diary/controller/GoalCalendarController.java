package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.goal.GoalCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalCalendarController {

    private final GoalCalendarService goalCalendarService;

    @PostMapping("/drop/entry-template")
    public ApiResponse<DiaryEntryGoalViewDto> createEntryGoal(
            @Valid @RequestBody DiaryEntryGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createEntryGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/drop/day-template")
    public ApiResponse<DayGoalViewDto> createDayGoal(
            @Valid @RequestBody DayGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createDayGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/drop/week-template")
    public ApiResponse<WeekGoalViewDto> createWeekGoal(
            @Valid @RequestBody WeekGoalCreateDtp dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createWeekGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/entry/{goalId}/confirm")
    public ApiResponse<DiaryEntryGoalViewDto> confirmEntryGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody DiaryEntryCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.confirmEntryGoal(user.getId(), goalId, dto));
    }

    @PutMapping("/entry/{goalId}")
    public ApiResponse<DiaryEntryGoalViewDto> updateConfirmedEntryGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody DiaryEntryUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.updateConfirmedEntryGoal(user.getId(), goalId, dto));
    }

    @PostMapping("/day/{dayGoalId}/confirm")
    public ApiResponse<DayGoalViewDto> confirmDay(
            @PathVariable Long dayGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalCalendarService.confirmDayGoal(user.getId(), dayGoalId));
    }
}
