package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Validated
@Slf4j
public class AdminDatabaseController {

    private final AdminDatabaseService adminDatabaseService;
    @Value("${app.admin.database.clear.enabled:false}")
    private boolean databaseClearEnabled;

    @GetMapping("/table-types")
    public ResponseEntity<ApiResponse<List<TableType>>> getTableTypes() {
        log.info("Admin database table types requested");
        return ResponseEntity.ok(
                ApiResponse.ok(TableType.allValues())
        );
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearDatabase() {
        ensureDatabaseClearEnabled();
        log.info("Admin database clear requested");
        int clearedTables = adminDatabaseService.clearAllTables();
        return ResponseEntity.ok(
                ApiResponse.okMessage("Cleared " + clearedTables + " tables")
        );
    }

    @PostMapping("/clear/{tableType}")
    public ResponseEntity<ApiResponse<Void>> clearTable(
            @PathVariable @NotBlank String tableType
    ) {
        ensureDatabaseClearEnabled();
        log.info("Admin database table clear requested: tableType={}", tableType);
        TableType resolvedTableType = TableType.fromValue(tableType);
        adminDatabaseService.clearTable(resolvedTableType);
        return ResponseEntity.ok(
                ApiResponse.okMessage("Cleared table " + resolvedTableType.getTableName())
        );
    }

    private void ensureDatabaseClearEnabled() {
        if (!databaseClearEnabled) {
            throw new BadRequestException("Admin database clear operations are disabled");
        }
    }
}
