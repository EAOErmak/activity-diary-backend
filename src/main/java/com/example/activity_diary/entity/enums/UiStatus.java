package com.example.activity_diary.entity.enums;

public enum UiStatus {
    FINISHED,        // Р·Р°РїРёСЃСЊ СѓСЃРїРµС€РЅРѕ РІС‹РїРѕР»РЅРµРЅР° (whenEnded < now)
    FAILED,
    ACTIVE,// Р·Р°РїРёСЃСЊ РЅРµ РІС‹РїРѕР»РЅРµРЅР° (whenEnded >= now)
    PLANNED,
    DELETED
}
