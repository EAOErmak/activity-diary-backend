package com.example.activity_diary.entity.enums;

public enum UiStatus {
    FINISHED,        // запись успешно выполнена (whenEnded < now)
    FAILED,
    ACTIVE,          // запись происходит сейчас
    PLANNED,
    OVERDUE,
    DELETED
}
