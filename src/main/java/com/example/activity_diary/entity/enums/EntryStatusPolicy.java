package com.example.activity_diary.entity.enums;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class EntryStatusPolicy {

    private static final Set<EntryStatus> TERMINAL_STATUSES =
            EnumSet.of(EntryStatus.FINISHED, EntryStatus.FAILED, EntryStatus.DELETED);

    private static final List<EntryStatus> OVERDUE_TRANSITION_SOURCE_STATUSES =
            List.of(EntryStatus.PLANNED, EntryStatus.ACTIVE);

    private EntryStatusPolicy() {
    }

    public static EntryStatus resolveAutomaticStatus(Instant whenStarted, Instant whenEnded, Instant now) {
        if (now.isBefore(whenStarted)) {
            return EntryStatus.PLANNED;
        }
        if (!now.isAfter(whenEnded)) {
            return EntryStatus.ACTIVE;
        }
        return EntryStatus.OVERDUE;
    }

    public static EntryStatus resolveCurrentStatus(
            EntryStatus currentStatus,
            Instant whenStarted,
            Instant whenEnded,
            Instant now
    ) {
        if (isTerminalStatus(currentStatus)) {
            return currentStatus;
        }
        return resolveAutomaticStatus(whenStarted, whenEnded, now);
    }

    public static boolean isTerminalStatus(EntryStatus status) {
        return status != null && TERMINAL_STATUSES.contains(status);
    }

    public static boolean canBeSetManually(EntryStatus status) {
        return status != EntryStatus.OVERDUE;
    }

    public static List<EntryStatus> overdueTransitionSourceStatuses() {
        return OVERDUE_TRANSITION_SOURCE_STATUSES;
    }
}
