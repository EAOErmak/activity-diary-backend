package com.example.activity_diary.entity.enums;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryStatusPolicyTest {

    @Test
    void resolveAutomaticStatus_futureEntry_returnsPlanned() {
        EntryStatus result = EntryStatusPolicy.resolveAutomaticStatus(
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T11:00:00Z")
        );

        assertEquals(EntryStatus.PLANNED, result);
    }

    @Test
    void resolveAutomaticStatus_currentEntry_returnsActive() {
        EntryStatus result = EntryStatusPolicy.resolveAutomaticStatus(
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T12:30:00Z")
        );

        assertEquals(EntryStatus.ACTIVE, result);
    }

    @Test
    void resolveAutomaticStatus_pastEntry_returnsOverdue() {
        EntryStatus result = EntryStatusPolicy.resolveAutomaticStatus(
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T13:00:01Z")
        );

        assertEquals(EntryStatus.OVERDUE, result);
    }

    @Test
    void resolveCurrentStatus_finishedEntry_staysFinished() {
        EntryStatus result = EntryStatusPolicy.resolveCurrentStatus(
                EntryStatus.FINISHED,
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T14:00:00Z")
        );

        assertEquals(EntryStatus.FINISHED, result);
    }

    @Test
    void resolveCurrentStatus_failedEntry_staysFailed() {
        EntryStatus result = EntryStatusPolicy.resolveCurrentStatus(
                EntryStatus.FAILED,
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T14:00:00Z")
        );

        assertEquals(EntryStatus.FAILED, result);
    }

    @Test
    void resolveCurrentStatus_deletedEntry_staysDeleted() {
        EntryStatus result = EntryStatusPolicy.resolveCurrentStatus(
                EntryStatus.DELETED,
                Instant.parse("2026-04-20T12:00:00Z"),
                Instant.parse("2026-04-20T13:00:00Z"),
                Instant.parse("2026-04-20T14:00:00Z")
        );

        assertEquals(EntryStatus.DELETED, result);
    }

    @Test
    void overdueTransitionSourceStatuses_includeOnlyPlannedAndActive() {
        assertEquals(
                List.of(EntryStatus.PLANNED, EntryStatus.ACTIVE),
                EntryStatusPolicy.overdueTransitionSourceStatuses()
        );
    }
}
