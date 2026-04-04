package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/database")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(value = "app.admin.database.clear.enabled", havingValue = "true")
public class AdminDatabaseController {

    private final AdminDatabaseService adminDatabaseService;

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearDatabase() {
        int clearedTables = adminDatabaseService.clearAllTables();
        return ResponseEntity.ok(
                ApiResponse.okMessage("Cleared " + clearedTables + " tables")
        );
    }
}
