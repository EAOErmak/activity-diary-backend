package com.example.activity_diary.controller;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.dto.mappers.DiaryEntryMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;
    private final UserService userService;
    private final DiaryEntryMapper mapper;

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<DiaryEntryDto>>> myEntries(@AuthenticationPrincipal UserDetails ud) {
        User user = userService.findByUsername(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        List<DiaryEntryDto> result = diaryService.getByUserId(user.getId())
                .stream()
                .map(mapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> getById(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserDetails ud) {
        var entry = diaryService.getByIdForUser(id, ud);
        return ResponseEntity.ok(ApiResponse.ok(mapper.toDto(entry)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiaryEntryDto>> create(@Valid @RequestBody DiaryEntryCreateDto dto,
                                                             @AuthenticationPrincipal UserDetails ud) {
        User user = userService.findByUsername(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        var entry = diaryService.create(dto, user);
        return ResponseEntity.ok(ApiResponse.ok(mapper.toDto(entry)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> update(@PathVariable Long id,
                                                             @Valid @RequestBody DiaryEntryUpdateDto dto,
                                                             @AuthenticationPrincipal UserDetails ud) {
        var entry = diaryService.update(id, dto, ud);
        return ResponseEntity.ok(ApiResponse.ok(mapper.toDto(entry)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails ud) {
        diaryService.delete(id, ud);
        return ResponseEntity.ok(ApiResponse.okMessage("Deleted successfully"));
    }

}
