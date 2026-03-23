package com.example.activity_diary.controller.template;


import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.day.DayTemplateUpdateDto;
import com.example.activity_diary.dto.template.day.DayTemplateViewDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.diary.DayTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/day-templates")
public class DayTemplateController {

    private final DayTemplateService service;

    @GetMapping
    public Page<DayTemplateViewDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.list(user.getId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DayTemplateViewDto create(
            @RequestBody DayTemplateCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.create(user.getId(), dto);
    }

    @GetMapping("/{id}")
    public DayTemplateViewDto get(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.get(user.getId(), id);
    }

    @PutMapping("/{id}")
    public DayTemplateViewDto update(
            @PathVariable Long id,
            @RequestBody DayTemplateUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.update(user.getId(), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        service.delete(user.getId(), id);
    }
}
