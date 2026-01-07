package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<TagDto>> getAll() {
        return  ApiResponse.ok(tagService.getAllTags());
    }

    @GetMapping("/search")
    public ApiResponse<List<TagDto>> search(@RequestParam String q) {
        return  ApiResponse.ok(tagService.searchTags(q));
    }
}
