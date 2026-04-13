package com.example.activity_diary.controller.template;

import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateUpdateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateViewDto;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.diary.WeekTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/week-templates")
public class WeekTemplateController {

    private final WeekTemplateService service;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Page<WeekTemplateViewDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(currentUserProvider.getCurrentUserId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeekTemplateViewDto create(@RequestBody WeekTemplateCreateDto dto) {
        return service.create(currentUserProvider.getCurrentUserId(), dto);
    }

    @GetMapping("/{id}")
    public WeekTemplateViewDto get(@PathVariable Long id) {
        return service.get(currentUserProvider.getCurrentUserId(), id);
    }

    @PutMapping("/{id}")
    public WeekTemplateViewDto update(
            @PathVariable Long id,
            @RequestBody WeekTemplateUpdateDto dto
    ) {
        return service.update(currentUserProvider.getCurrentUserId(), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUserProvider.getCurrentUserId(), id);
    }
}
