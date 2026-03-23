package com.example.activity_diary.controller.goal;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalCreateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalSummaryDto;
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
@RequestMapping("/api/goal/entry")
@RequiredArgsConstructor
public class EntryGoalController {

    private final GoalCalendarService goalCalendarService;
    private final GoalGetService goalGetService;

    @PostMapping("/drop")
    public ApiResponse<DiaryEntryGoalDetailDto> createEntryGoal(
            @Valid @RequestBody DiaryEntryGoalCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.createEntryGoal(user.getId(), dto.getTemplateId(), dto.getTargetDate())
        );
    }

    @PostMapping("/{goalId}/confirm")
    public DiaryEntryGoalDetailDto confirmReal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal LightUserDetails user,
            @RequestBody DiaryEntryCreateDto dto
    ) {
        return goalCalendarService.confirmEntryGoal(user.getId(), goalId, dto);
    }

    @PostMapping("/{goalId}/confirm-simple")
    public DiaryEntryGoalDetailDto confirmSimple(
            @PathVariable Long goalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return goalCalendarService.confirmEntryGoalSimple(user.getId(), goalId);
    }

    @PutMapping("/{goalId}")
    public ApiResponse<DiaryEntryGoalDetailDto> updateConfirmedEntryGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody DiaryEntryUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(
                goalCalendarService.updateConfirmedEntryGoal(user.getId(), goalId, dto)
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteEntry(
            @RequestParam Long entryGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        goalCalendarService.deleteEntryGoal(user.getId(), entryGoalId);
        return ApiResponse.ok();
    }

    // ===== Query =====

    @GetMapping("/summary/by-date")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listByDate(
            @RequestParam LocalDate date,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listEntrySummariesByDate(user.getId(), date));
    }

    @GetMapping("/summary/by-day-goal/{dayGoalId}")
    public ApiResponse<List<DiaryEntryGoalSummaryDto>> listByDayGoal(
            @PathVariable Long dayGoalId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.listEntrySummariesByDayGoal(user.getId(), dayGoalId));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<DiaryEntryGoalSummaryDto> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getEntryGoalSummary(user.getId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<DiaryEntryGoalDetailDto> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return ApiResponse.ok(goalGetService.getEntryGoalDetail(user.getId(), id));
    }
}
