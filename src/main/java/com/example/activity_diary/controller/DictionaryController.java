package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.dictionary.DictionaryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Validated
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/what-happened")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllWhatHappened(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllWhatHappened(ud)));
    }

    @GetMapping("/what")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getWhatByParent(
            @RequestParam @Positive Long parentId,
            @AuthenticationPrincipal UserDetails ud
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getWhatByParent(parentId, ud)));
    }

    @GetMapping("/item-name")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllItemNames(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllItemNames(ud)));
    }

    @GetMapping("/unit")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllUnits(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllUnits(ud)));
    }

    @GetMapping("/search")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.search(query)));
    }
}
