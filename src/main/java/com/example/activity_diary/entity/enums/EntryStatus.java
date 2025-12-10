package com.example.activity_diary.entity.enums;

public enum EntryStatus {

    DRAFT,     // запись создана, но ещё редактируется
    FINAL,     // запись зафиксирована
    DELETED;   // логически удалена

    // ============================================================
    // RULES
    // ============================================================

    public boolean canTransitionTo(EntryStatus target) {

        if (this == DELETED) {
            return false; // из удалённого никуда нельзя
        }

        if (this == DRAFT && target == FINAL) return true;
        if (this == FINAL && target == DELETED) return true;

        return false;
    }
}
