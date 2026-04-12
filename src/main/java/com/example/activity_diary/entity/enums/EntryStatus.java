package com.example.activity_diary.entity.enums;

public enum EntryStatus {
    FINISHED,        // запись успешно выполнена (whenEnded < now)
    FAILED,
    ACTIVE,// запись не выполнена (whenEnded >= now)
    PLANNED,
    DELETED     // логически удалена
}