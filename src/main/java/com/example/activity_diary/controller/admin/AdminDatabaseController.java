package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/database")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(value = "app.admin.database.clear.enabled", havingValue = "true")
@Validated
public class AdminDatabaseController {

    private final AdminDatabaseService adminDatabaseService;

    @GetMapping("/table-types")
    public ResponseEntity<ApiResponse<List<TableType>>> getTableTypes() {
        return ResponseEntity.ok(
                ApiResponse.ok(TableType.allValues())
        );
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearDatabase() {
        int clearedTables = adminDatabaseService.clearAllTables();
        return ResponseEntity.ok(
                ApiResponse.okMessage("Cleared " + clearedTables + " tables")
        );
    }

    @PostMapping("/clear/{tableType}")
    public ResponseEntity<ApiResponse<Void>> clearTable(
            @PathVariable @NotBlank String tableType
    ) {
        TableType resolvedTableType = TableType.fromValue(tableType);
        adminDatabaseService.clearTable(resolvedTableType);
        return ResponseEntity.ok(
                ApiResponse.okMessage("Cleared table " + resolvedTableType.getTableName())
        );
    }
}
