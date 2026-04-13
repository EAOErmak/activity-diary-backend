package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.food.UserFoodService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-foods")
@RequiredArgsConstructor
@Validated
public class UserFoodController {

    private final UserFoodService userFoodService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserFoodResponseDto>>> getAll(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.getAll(currentUserProvider.getCurrentUserId(), q))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.getById(currentUserProvider.getCurrentUserId(), id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> create(@Valid @RequestBody FoodUpsertDto dto) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.create(currentUserProvider.getCurrentUserId(), dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody FoodUpsertDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.update(currentUserProvider.getCurrentUserId(), id, dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Positive Long id) {
        userFoodService.delete(currentUserProvider.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
