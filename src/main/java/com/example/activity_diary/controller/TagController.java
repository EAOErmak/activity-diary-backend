package com.example.activity_diary.controller;

import com.example.activity_diary.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
}
