package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.platform.api.controller.admin.AdminDatabaseController;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDatabaseControllerTest {

    @Mock
    private AdminDatabaseService adminDatabaseService;

    @InjectMocks
    private AdminDatabaseController adminDatabaseController;

    @Test
    void getTableTypesReturnsAllEnumValuesWhenClearIsDisabled() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", false);

        ResponseEntity<ApiResponse<List<TableType>>> response = adminDatabaseController.getTableTypes();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactlyElementsOf(TableType.allValues());
    }

    @Test
    void getTableTypesReturnsAllEnumValues() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", true);

        ResponseEntity<ApiResponse<List<TableType>>> response = adminDatabaseController.getTableTypes();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactlyElementsOf(TableType.allValues());
    }

    @Test
    void clearDatabaseReturnsClearedTableCount() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", true);
        when(adminDatabaseService.clearAllTables()).thenReturn(7);

        ResponseEntity<ApiResponse<Void>> response = adminDatabaseController.clearDatabase();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Cleared 7 tables");
    }

    @Test
    void clearTableMapsIncomingValueToEnum() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", true);
        ResponseEntity<ApiResponse<Void>> response = adminDatabaseController.clearTable("day_goal");

        verify(adminDatabaseService).clearTable(TableType.DAY_GOAL);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Cleared table day_goal");
    }

    @Test
    void clearDatabaseRejectsRequestWhenClearIsDisabled() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", false);

        assertThatThrownBy(() -> adminDatabaseController.clearDatabase())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Admin database clear operations are disabled");
    }

    @Test
    void clearTableRejectsRequestWhenClearIsDisabled() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", false);

        assertThatThrownBy(() -> adminDatabaseController.clearTable("day_goal"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Admin database clear operations are disabled");
    }

    @Test
    void clearTableRejectsUnsupportedValue() {
        ReflectionTestUtils.setField(adminDatabaseController, "databaseClearEnabled", true);

        assertThatThrownBy(() -> adminDatabaseController.clearTable("unknown_table"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported table type: unknown_table");
    }
}
