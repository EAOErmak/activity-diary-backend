package com.example.activity_diary.controller.template;


import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.day.DayTemplateUpdateDto;
import com.example.activity_diary.dto.template.day.DayTemplateViewDto;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.diary.DayTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/day-templates")
public class DayTemplateController {

    private final DayTemplateService service;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Page<DayTemplateViewDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(currentUserProvider.getCurrentUserId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DayTemplateViewDto create(@RequestBody DayTemplateCreateDto dto) {
        return service.create(currentUserProvider.getCurrentUserId(), dto);
    }

    @GetMapping("/{id}")
    public DayTemplateViewDto get(@PathVariable Long id) {
        return service.get(currentUserProvider.getCurrentUserId(), id);
    }

    @PutMapping("/{id}")
    public DayTemplateViewDto update(
            @PathVariable Long id,
            @RequestBody DayTemplateUpdateDto dto
    ) {
        return service.update(currentUserProvider.getCurrentUserId(), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUserProvider.getCurrentUserId(), id);
    }
}
