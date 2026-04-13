package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Validated
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/all")
    public  ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAll() {
        Role role = currentUserProvider.getCurrentUser().getRole();

        return  ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getAll(role)
                )
        );
    }

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getForUser(@PathVariable DictionaryType type) {
        Role role = currentUserProvider.getCurrentUser().getRole();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getForUser(type, role)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(@RequestParam String query) {
        Role role = currentUserProvider.getCurrentUser().getRole();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.search(query, role)
                )
        );
    }
}
