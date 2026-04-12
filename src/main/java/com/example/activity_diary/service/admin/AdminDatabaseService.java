package com.example.activity_diary.service.admin;

import com.example.activity_diary.entity.enums.TableType;

public interface AdminDatabaseService {
    int clearAllTables();

    void clearTable(TableType tableType);
}
