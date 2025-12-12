package com.example.activity_diary.entity.enums;

public enum EntryStatus {
    WIN,        // запись успешно выполнена (whenEnded < now)
    LOSE,       // запись не выполнена (whenEnded >= now)
    DELETED     // логически удалена
}