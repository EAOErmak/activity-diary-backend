package com.example.activity_diary.controller;

import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.security.LightUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/templates")
public class ScheduleTemplateController {

    private final ScheduleTemplateService scheduleTemplateService;

    // -------------------------
    // CREATE
    // -------------------------

    @PostMapping("/day")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateViewDto createDay(
            @AuthenticationPrincipal LightUserDetails user,
            @RequestBody DayTemplateCreateDto dto
    ) {
        return scheduleTemplateService.createDayTemplate(user.getId(), dto);
    }

    @PostMapping("/week")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateViewDto createWeek(
            @AuthenticationPrincipal LightUserDetails user,
            @RequestBody WeekTemplateCreateDto dto
    ) {
        return scheduleTemplateService.createWeekTemplate(user.getId(), dto);
    }

    // -------------------------
    // UPDATE ITEMS
    // -------------------------

    @PutMapping("/{templateId}/day-items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDayItems(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId,
            @RequestBody TemplateItemsUpdateDto dto
    ) {
        scheduleTemplateService.updateDayTemplateItems(user.getId(), templateId, dto);
    }

    @PutMapping("/{templateId}/week-items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWeekItems(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId,
            @RequestBody TemplateItemsUpdateDto dto
    ) {
        scheduleTemplateService.updateWeekTemplateItems(user.getId(), templateId, dto);
    }

    // -------------------------
    // READ
    // -------------------------

    @GetMapping("/{templateId}")
    public TemplateViewDto get(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId
    ) {
        return scheduleTemplateService.getTemplate(user.getId(), templateId);
    }

    @GetMapping
    public Page<TemplateListItemDto> list(
            @AuthenticationPrincipal LightUserDetails user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return scheduleTemplateService.listTemplates(user.getId(), pageable);
    }

    // -------------------------
    // DELETE
    // -------------------------

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId
    ) {
        scheduleTemplateService.deleteTemplate(user.getId(), templateId);
    }
}
