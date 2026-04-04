package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.GeneralFoodResponseDto;
import com.example.activity_diary.service.food.GeneralFoodService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/general-foods")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminGeneralFoodController {

    private final GeneralFoodService generalFoodService;

    @PostMapping
    public ResponseEntity<ApiResponse<GeneralFoodResponseDto>> create(
            @Valid @RequestBody FoodUpsertDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(generalFoodService.create(dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneralFoodResponseDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody FoodUpsertDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(generalFoodService.update(id, dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive Long id
    ) {
        generalFoodService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
