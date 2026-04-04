package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.food.UserFoodService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-foods")
@RequiredArgsConstructor
@Validated
public class UserFoodController {

    private final UserFoodService userFoodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserFoodResponseDto>>> getAll(
            @AuthenticationPrincipal LightUserDetails user,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.getAll(user.getId(), q))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> getById(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.getById(user.getId(), id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> create(
            @AuthenticationPrincipal LightUserDetails user,
            @Valid @RequestBody FoodUpsertDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.create(user.getId(), dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserFoodResponseDto>> update(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable @Positive Long id,
            @Valid @RequestBody FoodUpsertDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(userFoodService.update(user.getId(), id, dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable @Positive Long id
    ) {
        userFoodService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
