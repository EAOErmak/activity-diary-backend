package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.exception.types.BadRequestException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDatabaseServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private AdminDatabaseServiceImpl adminDatabaseService;

    @Test
    void clearAllTablesClearsEveryWhitelistedTable() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int clearedTables = adminDatabaseService.clearAllTables();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager, times(TableType.values().length)).createNativeQuery(sqlCaptor.capture());
        verify(query, times(TableType.values().length)).executeUpdate();
        assertThat(clearedTables).isEqualTo(TableType.values().length);

        List<String> executedSql = sqlCaptor.getAllValues();
        assertThat(executedSql).hasSize(TableType.values().length);
        assertThat(executedSql).allMatch(sql -> sql.startsWith("TRUNCATE TABLE public."));
        assertThat(executedSql).contains(
                "TRUNCATE TABLE public.\"day_goal\" RESTART IDENTITY CASCADE",
                "TRUNCATE TABLE public.\"week_template\" RESTART IDENTITY CASCADE"
        );
    }

    @Test
    void clearTableClearsRequestedTableType() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        adminDatabaseService.clearTable(TableType.USER_FOOD);

        verify(entityManager).createNativeQuery(
                "TRUNCATE TABLE public.\"user_food\" RESTART IDENTITY CASCADE"
        );
        verify(query).executeUpdate();
    }

    @Test
    void clearTableRejectsNullTableType() {
        assertThatThrownBy(() -> adminDatabaseService.clearTable(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Table type is required");
    }
}
