package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.food.GeneralFoodResponseDto;
import com.example.activity_diary.service.food.GeneralFoodService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/general-foods")
@RequiredArgsConstructor
@Validated
public class GeneralFoodController {

    private final GeneralFoodService generalFoodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GeneralFoodResponseDto>>> getAll(
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(generalFoodService.getAll(q))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneralFoodResponseDto>> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(generalFoodService.getById(id))
        );
    }
}
