package com.example.activity_diary.controller.admin;

import com.example.activity_diary.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        tagService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        tagService.reject(id);
    }

    @PostMapping("/{id}/deprecate")
    public void deprecate(@PathVariable Long id) {
        tagService.deprecate(id);
    }
}
