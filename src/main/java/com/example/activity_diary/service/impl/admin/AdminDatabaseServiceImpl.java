package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.entity.enums.TableType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.admin.AdminDatabaseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDatabaseServiceImpl implements AdminDatabaseService {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public int clearAllTables() {
        for (TableType tableType : TableType.values()) {
            truncateTable(tableType);
        }

        return TableType.values().length;
    }

    @Override
    @Transactional
    public void clearTable(TableType tableType) {
        if (tableType == null) {
            throw new BadRequestException("Table type is required");
        }

        truncateTable(tableType);
    }

    private void truncateTable(TableType tableType) {
        truncateTable(tableType.getTableName());
    }

    private void truncateTable(String tableName) {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE public." + quoteIdentifier(tableName) + " RESTART IDENTITY CASCADE"
        ).executeUpdate();
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
